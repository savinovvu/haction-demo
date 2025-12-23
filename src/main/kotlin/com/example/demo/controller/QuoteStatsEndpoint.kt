package com.example.demo.controller

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
@Endpoint(id = "quotestats")
class QuoteStatsEndpoint {

    private val stats = ConcurrentHashMap<String, Any>()

    @ReadOperation
    fun getStats(): Map<String, Any> {
        val runtime = Runtime.getRuntime()
        val memoryBean = ManagementFactory.getMemoryMXBean()

        stats["timestamp"] = Instant.now().toString()
        stats["heap_used_mb"] = memoryBean.heapMemoryUsage.used / (1024 * 1024)
        stats["heap_max_mb"] = memoryBean.heapMemoryUsage.max / (1024 * 1024)
        stats["non_heap_used_mb"] = memoryBean.nonHeapMemoryUsage.used / (1024 * 1024)
        stats["uptime_seconds"] = ManagementFactory.getRuntimeMXBean().uptime / 1000
        stats["thread_count"] = ManagementFactory.getThreadMXBean().threadCount

        return stats
    }
}