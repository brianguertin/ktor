@file:Suppress("NOTHING_TO_INLINE")

package io.ktor.utils.io.bits

/**
 * Read short signed 16bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadShortAt(offset: Int): Short

/**
 * Read short signed 16bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadShortAt(offset: Long): Short

/**
 * Write short signed 16bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeShortAt(offset: Int, value: Short)

/**
 * Write short signed 16bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeShortAt(offset: Long, value: Short)

/**
 * Read short unsigned 16bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.loadUShortAt(offset: Int): UShort = loadShortAt(offset).toUShort()

/**
 * Read short unsigned 16bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.loadUShortAt(offset: Long): UShort = loadShortAt(offset).toUShort()

/**
 * Write short unsigned 16bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.storeUShortAt(offset: Int, value: UShort): Unit = storeShortAt(offset, value.toShort())

/**
 * Write short unsigned 16bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.storeUShortAt(offset: Long, value: UShort): Unit = storeShortAt(offset, value.toShort())

/**
 * Read regular signed 32bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadIntAt(offset: Int): Int

/**
 * Read regular signed 32bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadIntAt(offset: Long): Int

/**
 * Write regular signed 32bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeIntAt(offset: Int, value: Int)

/**
 * Write regular signed 32bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeIntAt(offset: Long, value: Int)

/**
 * Read regular unsigned 32bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.loadUIntAt(offset: Int): UInt = loadIntAt(offset).toUInt()

/**
 * Read regular unsigned 32bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.loadUIntAt(offset: Long): UInt = loadIntAt(offset).toUInt()

/**
 * Write regular unsigned 32bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.storeUIntAt(offset: Int, value: UInt): Unit = storeIntAt(offset, value.toInt())

/**
 * Write regular unsigned 32bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.storeUIntAt(offset: Long, value: UInt): Unit = storeIntAt(offset, value.toInt())

/**
 * Read short signed 64bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadLongAt(offset: Int): Long

/**
 * Read short signed 64bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadLongAt(offset: Long): Long

/**
 * Write short signed 64bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeLongAt(offset: Int, value: Long)

/**
 * write short signed 64bit integer in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeLongAt(offset: Long, value: Long)

/**
 * Read short signed 64bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.loadULongAt(offset: Int): ULong = loadLongAt(offset).toULong()

/**
 * Read short signed 64bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.loadULongAt(offset: Long): ULong = loadLongAt(offset).toULong()

/**
 * Write short signed 64bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.storeULongAt(offset: Int, value: ULong): Unit = storeLongAt(offset, value.toLong())

/**
 * Write short signed 64bit integer in the network byte order (Big Endian)
 */
public inline fun DROP_Memory.storeULongAt(offset: Long, value: ULong): Unit = storeLongAt(offset, value.toLong())

/**
 * Read short signed 32bit floating point number in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadFloatAt(offset: Int): Float

/**
 * Read short signed 32bit floating point number in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadFloatAt(offset: Long): Float

/**
 * Write short signed 32bit floating point number in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeFloatAt(offset: Int, value: Float)

/**
 * Write short signed 32bit floating point number in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeFloatAt(offset: Long, value: Float)

/**
 * Read short signed 64bit floating point number in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadDoubleAt(offset: Int): Double

/**
 * Read short signed 64bit floating point number in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.loadDoubleAt(offset: Long): Double

/**
 * Write short signed 64bit floating point number in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeDoubleAt(offset: Int, value: Double)

/**
 * Write short signed 64bit floating point number in the network byte order (Big Endian)
 */
public expect inline fun DROP_Memory.storeDoubleAt(offset: Long, value: Double)
