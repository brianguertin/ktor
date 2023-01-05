package io.ktor.io

public expect open class IOException(message: String, cause: Throwable?) : Exception {
    public constructor(message: String)
}

public expect open class EOFException(message: String) : IOException

/**
 * An exception thrown when an IO error occurred during reading or writing to/from the underlying channel.
 * The typical error is "connection reset" and so on.
 */
public open class ChannelIOException(message: String, exception: Throwable) : IOException(message, exception)

/**
 * An exception that is thrown when an IO error occurred during writing to the destination channel.
 * Usually it happens when a remote client closed the connection.
 */
public class ChannelWriteException(
    message: String = "Cannot write to a channel",
    exception: Throwable
) : ChannelIOException(message, exception)

/**
 * An exception that is thrown when an IO error occurred during reading from the request channel.
 * Usually it happens when a remote client closed the connection.
 */
public class ChannelReadException(
    message: String = "Cannot read from a channel",
    exception: Throwable
) : ChannelIOException(message, exception)

public class TooLongLineException(limit: Long) : MalformedInputException("The line is longer than limit $limit")

public open class MalformedInputException(message: String) : Throwable(message)
