package com.taskmanager.controller

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException::class)
    fun notFound(ex: EntityNotFoundException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not found")

    @ExceptionHandler(IllegalArgumentException::class)
    fun badRequest(ex: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Bad request")

    @ExceptionHandler(SecurityException::class)
    fun forbidden(ex: SecurityException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.message ?: "Forbidden")

    @ExceptionHandler(BadCredentialsException::class)
    fun unauthorized(ex: BadCredentialsException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(ex: MethodArgumentNotValidException): ProblemDetail {
        val errors = ex.bindingResult.allErrors.associate {
            ((it as? FieldError)?.field ?: "global") to (it.defaultMessage ?: "invalid")
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed")
            .also { it.setProperty("errors", errors) }
    }
}
