package ru.vgtu.diplom.integration.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties("service.orchestrator")
data class OrchestratorProperty(
    val baseUrl: String,
    val lockDuration: Duration,
    val asyncResponseTimeout: Duration
)