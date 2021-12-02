package ch.post.logistics.mailpiece.processor.config

import org.apache.kafka.streams.errors.UnknownStateStoreException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ControllerExceptionHandler {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentException() {
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Resource not found")
    @ExceptionHandler(NoSuchElementException::class)
    fun noSuchElementException() {
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Unknown state store")
    @ExceptionHandler(UnknownStateStoreException::class)
    fun unknownStateStoreException() {
    }

}