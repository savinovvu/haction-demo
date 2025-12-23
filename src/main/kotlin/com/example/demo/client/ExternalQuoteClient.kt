package com.example.demo.client

import com.example.demo.model.ExternalQuote
import feign.RequestInterceptor
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import java.util.UUID

@FeignClient(
    name = "externalQuoteService",
    url = "\${external.quote.service.url:http://hackathon-quote-service:8080}",
    configuration = [FeignConfig::class]
)
interface ExternalQuoteClient {

    @GetMapping("/quotes/{id}")
    fun getQuote(
        @PathVariable id: String,
        @RequestHeader("X-Request-Id") requestId: String
    ): ExternalQuote
}

@Configuration
class FeignConfig {

    @Bean
    fun feignRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val requestId = RequestContextHolder.getRequestAttributes()
                ?.let { it.getAttribute("X-Request-Id", RequestAttributes.SCOPE_REQUEST) as? String }
                ?: UUID.randomUUID().toString()

            template.header("X-Request-Id", requestId)
            template.header("Accept", "application/json")
        }
    }
}