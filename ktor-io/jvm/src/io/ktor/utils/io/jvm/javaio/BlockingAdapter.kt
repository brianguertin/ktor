/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io.jvm.javaio

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.slf4j.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

private val ADAPTER_LOGGER by lazy { LoggerFactory.getLogger(BlockingAdapter::class.java) }

internal abstract class BlockingAdapter(val parent: Job? = null) {

    private val end: Continuation<Unit> = object : Continuation<Unit> {
        override val context: CoroutineContext =
            if (parent != null) UnsafeBlockingTrampoline + parent else UnsafeBlockingTrampoline

        override fun resumeWith(result: Result<Unit>) {
            val value = result.exceptionOrNull() ?: Unit

            val before = state.getAndUpdate { current ->
                when (current) {
                    is Thread, is Continuation<*>, this -> value
                    else -> return
                }
            }

            when (before) {
                is Thread -> parkingImpl.unpark(before)
                is Continuation<*> -> result.exceptionOrNull()?.let {
                    before.resumeWithException(it)
                }
            }

            if (result.isFailure && result.exceptionOrNull() !is CancellationException) {
                parent?.cancel()
            }

            disposable?.dispose()
        }
    }

    @Suppress("LeakingThis")
    private val state: AtomicRef<Any> =
        atomic(this) // could be a thread, a continuation, Unit, an exception or this if not yet started
    private val result = atomic(0)
    private val disposable: DisposableHandle? = parent?.invokeOnCompletion { cause ->
        if (cause != null) {
            end.resumeWithException(cause)
        }
    }

    protected var offset: Int = 0
        private set
    protected var length: Int = 0
        private set

    init {
        val block: suspend () -> Unit = { loop() }
        block.startCoroutineUninterceptedOrReturn(end)
        require(state.value !== this)
    }

    protected abstract suspend fun loop()

    fun shutdown() {
        disposable?.dispose()
        end.resumeWithException(CancellationException("Stream closed"))
    }

    fun submitAndAwait(buffer: ByteArray, offset: Int, length: Int): Int {
        this.offset = offset
        this.length = length
        return submitAndAwait(buffer)
    }

    fun submitAndAwait(jobToken: Any): Int {
        val thread = Thread.currentThread()

        var cont: Continuation<Any>? = null

        state.update { value ->
            when (value) {
                is Continuation<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    cont = value as Continuation<Any>
                    thread
                }

                is Unit -> {
                    return result.value
                }

                is Throwable -> {
                    throw value
                }

                is Thread -> throw IllegalStateException("There is already thread owning adapter")
                this -> throw IllegalStateException("Not yet started")
                else -> NoWhenBranchMatchedException()
            }
        }

        cont!!.resume(jobToken)

        parkingLoop(thread)

        state.value.let { state ->
            if (state is Throwable) {
                throw state
            }
        }

        return result.value
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun parkingLoop(thread: Thread) {
        if (state.value !== thread) return
        if (!isParkingAllowed()) {
            ADAPTER_LOGGER.warn(
                """
                Blocking network thread detected. 
                It can possible lead to a performance decline or even a deadlock.
                Please make sure you're using blocking IO primitives like InputStream and OutputStream only in 
                the context of Dispatchers.IO:
                ```
                withContext(Dispatchers.IO) {
                    myInputStream.read()
                }
                ```
                """.trimIndent()
            )
        }

        do {
            val nextEventTimeNanos = processNextEventInCurrentThread()
            if (state.value !== thread) break

            if (nextEventTimeNanos > 0L) {
                parkingImpl.park(nextEventTimeNanos)
            }
        } while (true)
    }

    @Suppress("NOTHING_TO_INLINE")
    protected suspend inline fun rendezvous(rc: Int): Any {
        result.value = rc

        return suspendCoroutineUninterceptedOrReturn { ucont ->
            rendezvousBlock(ucont)
        }
    }

    private fun rendezvousBlock(ucont: Continuation<Any>): Any {
        var thread: Thread? = null

        state.update { value ->
            when (value) {
                is Thread -> {
                    thread = value
                    ucont.intercepted()
                }

                this -> ucont.intercepted()
                else -> throw IllegalStateException("Already suspended or in finished state")
            }
        }

        if (thread != null) {
            parkingImpl.unpark(thread!!)
        }

        return COROUTINE_SUSPENDED
    }

    protected fun finish(rc: Int) {
        result.value = rc
    }
}

private object UnsafeBlockingTrampoline : CoroutineDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext): Boolean = true

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }
}
