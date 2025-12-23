package com.example.demo.service

import com.example.demo.client.ExternalQuoteClient
import com.example.demo.model.Quote
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class QuoteCacheService(
    private val externalQuoteClient: ExternalQuoteClient,
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(QuoteCacheService::class.java)

    private val quotesCache = ConcurrentHashMap<String, Quote>()

    private lateinit var externalApiCallsCounter: Counter

    @PostConstruct
    fun initMetrics() {
        externalApiCallsCounter = Counter.builder("external_api_calls_total")
            .description("Total calls to external quote service")
            .tag("status", "success")
            .register(meterRegistry)

        Counter.builder("external_api_calls_total")
            .description("Total calls to external quote service")
            .tag("status", "error")
            .register(meterRegistry)
    }

    fun getQuote(id: String, requestId: String): Quote {
        return quotesCache[id]?.let {
            logger.info("Cache hit for quote id: {}", id)
            it
        } ?: run {
            logger.info("Cache miss for quote id: {}, fetching from external service", id)
            fetchAndCacheQuote(id, requestId)
        }
    }

    private fun fetchAndCacheQuote(id: String, requestId: String): Quote {
        // Простая синхронизация по всему кэшу для данного ID
        synchronized(quotesCache) {
            // Double-check после получения блокировки
            quotesCache[id]?.let { return it }

            try {
                val externalQuote = externalQuoteClient.getQuote(id, requestId)
                externalApiCallsCounter.increment()

                val quote = Quote(
                    id = externalQuote.id,
                    text = externalQuote.text,
                    author = externalQuote.author
                )

                quotesCache[quote.id] = quote
                logger.info("Cached quote id: {}", quote.id)

                return quote
            } catch (e: Exception) {
                meterRegistry.counter("external_api_calls_total", "status", "error").increment()
                throw ExternalServiceException("Failed to fetch quote from external service: ${e.message}")
            }
        }
    }

    fun getAllQuotes(): List<Quote> = quotesCache.values.toList()

    fun clearCache() {
        quotesCache.clear()
        logger.info("Cache cleared")
    }

    fun getCacheSize(): Int = quotesCache.size

    fun getCachedQuote(id: String): Quote? = quotesCache[id]
}

class ExternalServiceException(message: String) : RuntimeException(message)