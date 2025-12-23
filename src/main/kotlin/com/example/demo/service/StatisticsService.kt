package com.example.demo.service

import com.example.demo.model.Quote
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class StatisticsService(
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(StatisticsService::class.java)

    private val userLikes = ConcurrentHashMap<String, MutableSet<String>>()

    private lateinit var quoteViewsCounter: Counter
    private lateinit var quoteLikesCounter: Counter

    @PostConstruct
    fun initMetrics() {
        quoteViewsCounter = Counter.builder("quote_views_total")
            .description("Total number of quote views")
            .register(meterRegistry)

        quoteLikesCounter = Counter.builder("quote_likes_total")
            .description("Total number of quote likes")
            .register(meterRegistry)
    }

    fun addView(quote: Quote) {
        quote.likes.incrementAndGet()

        quoteViewsCounter.increment()

        // Update quote-specific counter
        meterRegistry.counter("quote_views_by_id", "quote_id", quote.id).increment()
        logger.debug("Incremented views for quote id: {}, total views: {}", quote.id, quote.views)
    }

    fun addLike(quoteId: String, userId: String, quote: Quote): Boolean {
        val userLikedQuotes = userLikes.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet<String>() }

        return if (userLikedQuotes.contains(quoteId)) {
            logger.info("User {} already liked quote {}", userId, quoteId)
            false
        } else {
            userLikedQuotes.add(quoteId)
            quote.likes.incrementAndGet()
            quoteLikesCounter.increment()

            // Update quote-specific counter
            meterRegistry.counter("quote_likes_by_id", "quote_id", quoteId).increment()

            logger.info("User {} liked quote {}, total likes: {}", userId, quoteId, quote.likes)
            true
        }
    }

    fun removeLike(quoteId: String, userId: String, quote: Quote): Boolean {
        val userLikedQuotes = userLikes[userId] ?: return false

        return if (userLikedQuotes.remove(quoteId)) {
            quote.likes.decrementAndGet()

            if (userLikedQuotes.isEmpty()) {
                userLikes.remove(userId)
            }

            logger.info("User {} removed like from quote {}, total likes: {}", userId, quoteId, quote.likes)
            true
        } else {
            logger.info("User {} didn't like quote {}", userId, quoteId)
            false
        }
    }

    fun hasUserLiked(userId: String, quoteId: String): Boolean {
        return userLikes[userId]?.contains(quoteId) ?: false
    }

    fun getUserLikes(userId: String): Set<String> {
        return userLikes[userId]?.toSet() ?: emptySet()
    }

    fun getTotalLikes(): Long = userLikes.values.sumOf { it.size.toLong() }

    fun getTotalUsers(): Int = userLikes.size

    fun clearStatistics() {
        userLikes.clear()
        logger.info("Statistics cleared")
    }
}