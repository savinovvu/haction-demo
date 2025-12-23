package com.example.demo.exceptionhandler

import com.example.demo.model.ApiError
import com.example.demo.service.ExternalServiceException
import com.example.demo.service.LikeAlreadyExistsException
import com.example.demo.service.LikeNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.warn("Bad request: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiError(
                    error = "invalid_parameter",
                    message = ex.message ?: "Invalid request parameter",
                    requestId = request.getAttribute("X-Request-Id") as? String
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        logger.warn("Validation error: {}", message)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiError(
                    error = "validation_error",
                    message = message,
                    requestId = request.getAttribute("X-Request-Id") as? String
                )
            )
    }

    @ExceptionHandler(LikeAlreadyExistsException::class)
    fun handleLikeAlreadyExistsException(
        ex: LikeAlreadyExistsException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.info("Like conflict: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ApiError(
                    error = "like_already_exists",
                    message = ex.message ?: "User has already liked this quote",
                    requestId = request.getAttribute("X-Request-Id") as? String
                )
            )
    }

    @ExceptionHandler(LikeNotFoundException::class)
    fun handleLikeNotFoundException(
        ex: LikeNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.info("Like not found: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiError(
                    error = "like_not_found",
                    message = ex.message ?: "Like not found",
                    requestId = request.getAttribute("X-Request-Id") as? String
                )
            )
    }

    @ExceptionHandler(ExternalServiceException::class)
    fun handleExternalServiceException(
        ex: ExternalServiceException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.error("External service error: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiError(
                    error = "quote_not_found",
                    message = ex.message ?: "Quote not found in external service",
                    requestId = request.getAttribute("X-Request-Id") as? String
                )
            )
    }

    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    fun handleQuoteNotFoundException(
        ex: HttpClientErrorException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.info("Quote not found in external service: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiError(
                    error = "quote_not_found",
                    message = "Quote not found in external service",
                    requestId = request.getAttribute("X-Request-Id") as? String
                )
            )
    }

    @ExceptionHandler(ResourceAccessException::class)
    fun handleResourceAccessException(
        ex: ResourceAccessException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.error("Connection error to external service: {}", ex.message)

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(
                ApiError(
                    error = "external_service_unavailable",
                    message = "External quote service is unavailable",
                    requestId = request.getAttribute("X-Request-Id") as? String
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        logger.error("Internal server error", ex)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiError(
                    error = "internal_server_error",
                    message = "An unexpected error occurred",
                    requestId = request.getAttribute("X-Request-Id") as? String
                )
            )
    }
}