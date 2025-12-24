package com.example.demo.service

import com.example.demo.model.LikeResponse
import com.example.demo.model.QuoteResponse
import com.example.demo.model.TopQuotesResponse
import org.springframework.stereotype.Service

@Service
class QuoteService(
    private val quoteCacheService: QuoteCacheService,
    private val statisticsService: StatisticsService
) {

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
                allQuotes.sortedBy { it.views.get() }
            else
                allQuotes.sortedByDescending { it.views.get() }
            else -> if (order.equals("asc", true))
                allQuotes.sortedBy { it.likes.get() }
            else
                allQuotes.sortedByDescending { it.likes.get() }
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
                likes = quote.likes.get(),
                views = quote.views.get(),
                userLiked = userId?.let { statisticsService.hasUserLiked(it, quote.id) }
            )
        }
    }

    fun getQuote(id: String, userId: String? = null, reqId: String): QuoteResponse {
        val quote = quoteCacheService.getQuote(id, reqId)

        // Increment views
        statisticsService.addView(quote)

        return QuoteResponse(
            id = quote.id,
            text = quote.text,
            author = quote.author,
            likes = quote.likes.get(),
            views = quote.views.get(),
            userLiked = userId?.let { statisticsService.hasUserLiked(it, quote.id) }
        )
    }

    fun getTopQuotes(count: Int = 10, userId: String? = null): TopQuotesResponse {
        val allQuotes = quoteCacheService.getAllQuotes()

        val topQuotes = allQuotes
            .sortedByDescending { it.likes.get() }
            .take(count)
            .map { quote ->
                // Increment views for top quotes
                statisticsService.addView(quote)

                QuoteResponse(
                    id = quote.id,
                    text = quote.text,
                    author = quote.author,
                    likes = quote.likes.get(),
                    views = quote.views.get(),
                    userLiked = userId?.let { statisticsService.hasUserLiked(it, quote.id) }
                )
            }

        return TopQuotesResponse(
            topQuotes = topQuotes,
            totalCount = allQuotes.size
        )
    }

    fun likeQuote(quoteId: String, userId: String, reqId: String): LikeResponse {
        val quote = quoteCacheService.getQuote(quoteId, reqId)

        val success = statisticsService.addLike(quoteId, userId, quote)

        return if (success) {
            LikeResponse(
                success = true,
                message = "like_added_successfully",
                quoteId = quoteId,
                userId = userId,
                likesCount = quote.likes.get(),
                userLiked = true
            )
        } else {
            throw LikeAlreadyExistsException("User '$userId' has already liked this quote")
        }
    }

    fun unlikeQuote(quoteId: String, userId: String, reqId: String): LikeResponse {
        val quote = quoteCacheService.getQuote(quoteId, reqId)

        val success = statisticsService.removeLike(quoteId, userId, quote)

        return if (success) {
            LikeResponse(
                success = true,
                message = "like_removed_successfully",
                quoteId = quoteId,
                userId = userId,
                likesCount = quote.likes.get(),
                userLiked = false
            )
        } else {
            throw LikeNotFoundException("User '$userId' has not liked this quote")
        }
    }
}

class LikeAlreadyExistsException(message: String) : RuntimeException(message)
class LikeNotFoundException(message: String) : RuntimeException(message)