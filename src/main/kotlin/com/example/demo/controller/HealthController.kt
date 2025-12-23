package com.example.demo.controller

import com.example.demo.model.HealthResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.management.ManagementFactory

@RestController
@RequestMapping("/health")
class HealthController(
) {

    @GetMapping("/liveness")
    fun liveness(): ResponseEntity<HealthResponse> {
        val uptime = ManagementFactory.getRuntimeMXBean().uptime / 1000

        return ResponseEntity.ok(
            HealthResponse(
                status = "up",
                uptimeSeconds = uptime
            )
        )
    }

    @GetMapping("/readiness")
    fun readiness(): ResponseEntity<Any> {
        return ResponseEntity.ok("ok")
    }
}