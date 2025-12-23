package com.example.demo.controller

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean

@RestController
@RequestMapping("/metrics")
class MetricsController(
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(MetricsController::class.java)
    private val memoryBean: MemoryMXBean = ManagementFactory.getMemoryMXBean()

    @GetMapping
    fun getCustomMetrics(): ResponseEntity<String> {
        val metrics = StringBuilder()

        // Добавляем метрики использования памяти
        val usedMemory = (memoryBean.heapMemoryUsage.used + memoryBean.nonHeapMemoryUsage.used) / (1024.0 * 1024.0)
        metrics.append("# HELP service_memory_usage_bytes Current memory usage of the service\n")
        metrics.append("# TYPE service_memory_usage_bytes gauge\n")
        metrics.append("service_memory_usage_bytes ${usedMemory}\n\n")

        // Метрика uptime
        val uptime = ManagementFactory.getRuntimeMXBean().uptime / 1000.0
        metrics.append("# HELP service_uptime_seconds Service uptime in seconds\n")
        metrics.append("# TYPE service_uptime_seconds counter\n")
        metrics.append("service_uptime_seconds $uptime\n\n")

        // Статистика по кэшу цитат
        val quoteCacheSize = meterRegistry.find("cache.quotes.size")?.gauge()?.value() ?: 0.0
        metrics.append("# HELP cache_quotes_size Number of cached quotes\n")
        metrics.append("# TYPE cache_quotes_size gauge\n")
        metrics.append("cache_quotes_size $quoteCacheSize\n\n")

        // Статистика по пользователям
        val activeUsers = meterRegistry.find("users.active.count")?.gauge()?.value() ?: 0.0
        metrics.append("# HELP users_active_count Number of active users with likes\n")
        metrics.append("# TYPE users_active_count gauge\n")
        metrics.append("users_active_count $activeUsers\n")

        return ResponseEntity.ok(metrics.toString())
    }
}