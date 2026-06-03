package com.apistarter.health

import com.apistarter.common.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/health")
class HealthController {
    @GetMapping
    fun health(): ApiResponse<HealthResponse> = ApiResponse.success(
        message = "API is healthy",
        data = HealthResponse(
            status = "UP",
            timestamp = Instant.now(),
        ),
    )
}

data class HealthResponse(
    val status: String,
    val timestamp: Instant,
)
