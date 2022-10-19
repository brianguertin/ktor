package io.ktor.utils.io

import io.ktor.utils.io.core.internal.*

class ByteChannelSequentialScenarioTest : ByteBufferChannelScenarioTest() {

    override fun ByteChannel(autoFlush: Boolean): ByteChannel = ByteChannelSequentialJVM(DROP_ChunkBuffer.Empty, autoFlush)
}
