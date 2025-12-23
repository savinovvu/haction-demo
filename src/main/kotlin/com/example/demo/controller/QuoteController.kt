package com.example.demo.controller

import com.example.demo.model.ApiError
import com.example.demo.service.ExternalServiceException
import com.example.demo.service.LikeAlreadyExistsException
import com.example.demo.service.QuoteService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import com.example.demo.model.LikeRequest
import com.example.demo.service.LikeNotFoundException

@RestController
@RequestMapping("/quotes")
@Validated
class QuoteController(
    private val quoteService: QuoteService
) {
    private val logger = LoggerFactory.getLogger(QuoteController::class.java)

    @GetMapping
    fun getQuotes(
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) limit: Int,
        @RequestParam(defaultValue = "0") @Min(0) offset: Int,
        @RequestParam(defaultValue = "likes") sortBy: String,
        @RequestParam(defaultValue = "desc") order: String,
        @RequestParam(name = "user_id", required = false) userId: String?,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        logger.info("GET /quotes - limit: {}, offset: {}, sortBy: {}, order: {}, userId: {}",
            limit, offset, sortBy, order, userId)

        try {
            validateSortParams(sortBy, order)

            val quotes = quoteService.getQuotes(limit, offset, sortBy, order, userId)
            return ResponseEntity.ok(quotes)
        } catch (e: IllegalArgumentException) {
            val error = ApiError(
                error = "invalid_parameter",
                message = e.message ?: "Invalid parameter",
                requestId = request.getAttribute("X-Request-Id") as? String
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
        }
    }

    @GetMapping("/{id}")
    fun getQuote(
        @PathVariable id: String,
        @RequestHeader(name = "X-User-Id", required = false) userId: String?,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        logger.info("GET /quotes/{} - userId: {}", id, userId)

        try {
            val quote = quoteService.getQuote(id, userId)
            return ResponseEntity.ok(quote)
        } catch (e: ExternalServiceException) {
            val error = ApiError(
                error = "quote_not_found",
                message = "Quote with id '$id' not found",
                requestId = request.getAttribute("X-Request-Id") as? String
            )
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

    @GetMapping("/top")
    fun getTopQuotes(
        @RequestParam(defaultValue = "10") @Min(1) @Max(50) count: Int,
        @RequestParam(name = "user_id", required = false) userId: String?,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        logger.info("GET /quotes/top - count: {}, userId: {}", count, userId)

        try {
            val topQuotes = quoteService.getTopQuotes(count, userId)
            return ResponseEntity.ok(topQuotes)
        } catch (e: IllegalArgumentException) {
            val error = ApiError(
                error = "invalid_parameter",
                message = e.message ?: "Invalid parameter",
                requestId = request.getAttribute("X-Request-Id") as? String
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
        }
    }

    @PostMapping("/{id}/like")
    fun likeQuote(
        @PathVariable id: String,
        @Valid @RequestBody likeRequest: LikeRequest,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        logger.info("POST /quotes/{}/like - userId: {}", id, likeRequest.userId)

        try {
            val response = quoteService.likeQuote(id, likeRequest.userId)
            return ResponseEntity.ok(response)
        } catch (e: LikeAlreadyExistsException) {
            val error = ApiError(
                error = "like_already_exists",
                message = e.message ?: "User has already liked this quote",
                requestId = request.getAttribute("X-Request-Id") as? String
            )
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
        } catch (e: ExternalServiceException) {
            val error = ApiError(
                error = "quote_not_found",
                message = "Quote with id '$id' not found",
                requestId = request.getAttribute("X-Request-Id") as? String
            )
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

    @DeleteMapping("/{id}/like")
    fun unlikeQuote(
        @PathVariable id: String,
        @Valid @RequestBody likeRequest: LikeRequest,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        logger.info("DELETE /quotes/{}/like - userId: {}", id, likeRequest.userId)

        try {
            val response = quoteService.unlikeQuote(id, likeRequest.userId)
            return ResponseEntity.ok(response)
        } catch (e: LikeNotFoundException) {
            val error = ApiError(
                error = "like_not_found",
                message = e.message ?: "User has not liked this quote",
                requestId = request.getAttribute("X-Request-Id") as? String
            )
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        } catch (e: ExternalServiceException) {
            val error = ApiError(
                error = "quote_not_found",
                message = "Quote with id '$id' not found",
                requestId = request.getAttribute("X-Request-Id") as? String
            )
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

    private fun validateSortParams(sortBy: String, order: String) {
        if (sortBy !in listOf("likes", "views")) {
            throw IllegalArgumentException("Parameter 'sort_by' must be either 'likes' or 'views'")
        }
        if (order !in listOf("asc", "desc")) {
            throw IllegalArgumentException("Parameter 'order' must be either 'asc' or 'desc'")
        }
    }
}