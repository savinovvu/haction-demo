package com.example.demo.service

import com.example.demo.model.LikeResponse
import com.example.demo.model.QuoteResponse
import com.example.demo.model.TopQuotesResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class QuoteService(
    private val quoteCacheService: QuoteCacheService,
    private val statisticsService: StatisticsService
) {
    private val logger = LoggerFactory.getLogger(QuoteService::class.java)

    fun getQuotes(
        limit: Int = 10,
        offset: Int = 0,
        sortBy: String = "likes",
        order: String = "desc",
        userId: String? = null
    ): List<QuoteResponse> {
        val allQuotes = quoteCacheService.getAllQuotes()

        // Sort quotes
        val sortedQuotes = when (sortBy.lowercase()) {
            "views" -> if (order.equals("asc", true))
                allQuotes.sortedBy { it.views }
            else
                allQuotes.sortedByDescending { it.views }
            else -> if (order.equals("asc", true))
                allQuotes.sortedBy { it.likes }
            else
                allQuotes.sortedByDescending { it.likes }
        }

        // Apply pagination
        val paginatedQuotes = sortedQuotes
            .drop(offset)
            .take(limit)

        // Increment views and convert to response
        return paginatedQuotes.map { quote ->
            statisticsService.addView(quote)

            QuoteResponse(
                id = quote.id,
                text = quote.text,
                author = quote.author,
                likes = quote.likes,
                views = quote.views,
                userLiked = userId?.let { statisticsService.hasUserLiked(it, quote.id) }
            )
        }
    }

    fun getQuote(id: String, userId: String? = null): QuoteResponse {
        val requestId = UUID.randomUUID().toString()
        val quote = quoteCacheService.getQuote(id, requestId)

        // Increment views
        statisticsService.addView(quote)

        return QuoteResponse(
            id = quote.id,
            text = quote.text,
            author = quote.author,
            likes = quote.likes,
            views = quote.views,
            userLiked = userId?.let { statisticsService.hasUserLiked(it, quote.id) }
        )
    }

    fun getTopQuotes(count: Int = 10, userId: String? = null): TopQuotesResponse {
        val allQuotes = quoteCacheService.getAllQuotes()

        val topQuotes = allQuotes
            .sortedByDescending { it.likes }
            .take(count)
            .map { quote ->
                // Increment views for top quotes
                statisticsService.addView(quote)

                QuoteResponse(
                    id = quote.id,
                    text = quote.text,
                    author = quote.author,
                    likes = quote.likes,
                    views = quote.views,
                    userLiked = userId?.let { statisticsService.hasUserLiked(it, quote.id) }
                )
            }

        return TopQuotesResponse(
            topQuotes = topQuotes,
            totalCount = allQuotes.size
        )
    }

    fun likeQuote(quoteId: String, userId: String): LikeResponse {
        val requestId = UUID.randomUUID().toString()
        val quote = quoteCacheService.getQuote(quoteId, requestId)

        val success = statisticsService.addLike(quoteId, userId, quote)

        return if (success) {
            LikeResponse(
                success = true,
                message = "like_added_successfully",
                quoteId = quoteId,
                userId = userId,
                likesCount = quote.likes,
                userLiked = true
            )
        } else {
            throw LikeAlreadyExistsException("User '$userId' has already liked this quote")
        }
    }

    fun unlikeQuote(quoteId: String, userId: String): LikeResponse {
        val requestId = UUID.randomUUID().toString()
        val quote = quoteCacheService.getQuote(quoteId, requestId)

        val success = statisticsService.removeLike(quoteId, userId, quote)

        return if (success) {
            LikeResponse(
                success = true,
                message = "like_removed_successfully",
                quoteId = quoteId,
                userId = userId,
                likesCount = quote.likes,
                userLiked = false
            )
        } else {
            throw LikeNotFoundException("User '$userId' has not liked this quote")
        }
    }
}

class LikeAlreadyExistsException(message: String) : RuntimeException(message)
class LikeNotFoundException(message: String) : RuntimeException(message)