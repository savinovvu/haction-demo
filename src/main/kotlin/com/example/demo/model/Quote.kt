package com.example.demo.model

import java.time.Instant

data class ExternalQuote(
    val id: String,
    val text: String,
    val author: String
)

data class Quote(
    val id: String,
    val text: String,
    val author: String,
    var likes: Long = 0,
    var views: Long = 0,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
) {
    fun incrementViews() {
        views++
        updatedAt = Instant.now()
    }

    fun incrementLikes() {
        likes++
        updatedAt = Instant.now()
    }

    fun decrementLikes() {
        if (likes > 0) likes--
        updatedAt = Instant.now()
    }
}

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