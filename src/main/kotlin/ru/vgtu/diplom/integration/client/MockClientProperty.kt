package ru.vgtu.diplom.integration.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("mock")
data class MockClientProperty(
    val baseUrl: String
)