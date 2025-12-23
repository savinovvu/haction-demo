package com.example.demo.model

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

data class ExternalQuote(
    val id: String,
    val text: String,
    val author: String
)

data class Quote(
    val id: String,
    val text: String,
    val author: String,
    var likes: AtomicLong = AtomicLong(0),
    var views: AtomicLong = AtomicLong(0),
)

data class QuoteResponse(
    val id: String,
    val text: String,
    val author: String,
    val likes: Long,
    val views: Long,
    val userLiked: Boolean? = null
)

data class LikeRequest(
    val userId: String
)

data class LikeResponse(
    val success: Boolean,
    val message: String,
    val quoteId: String,
    val userId: String,
    val likesCount: Long,
    val userLiked: Boolean,
    val timestamp: Instant = Instant.now()
)

data class TopQuotesResponse(
    val topQuotes: List<QuoteResponse>,
    val totalCount: Int,
    val generatedAt: Instant = Instant.now()
)

data class ApiError(
    val error: String,
    val message: String,
    val requestId: String?,
    val timestamp: Instant = Instant.now()
)

data class HealthResponse(
    val status: String,
    val service: String = "quote_stats_service",
    val version: String = "1.0.0",
    val timestamp: Instant = Instant.now(),
    val uptimeSeconds: Long
)

data class ReadinessResponse(
    val status: String,
    val timestamp: Instant = Instant.now(),
    val checks: Map<String, HealthCheck>
)

data class HealthCheck(
    val status: String,
    val details: Map<String, Any> = emptyMap()
)