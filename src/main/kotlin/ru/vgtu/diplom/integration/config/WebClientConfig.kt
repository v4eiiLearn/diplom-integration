package ru.vgtu.diplom.integration.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import ru.vgtu.diplom.common.webclient.MdcWebClientFactory
import ru.vgtu.diplom.integration.client.MockClient
import ru.vgtu.diplom.integration.client.MockClientProperty

@Configuration
@EnableConfigurationProperties(MockClientProperty::class)
class WebClientConfig(
    private val mockClientProperty: MockClientProperty
) {

    @Bean
    fun webClient() : WebClient =
        MdcWebClientFactory.DEFAULT_INSTANCE

    @Bean
    fun mockClient(webClient: WebClient) : MockClient = with(mockClientProperty) {
        MockClient(webClient.mutate().baseUrl(baseUrl).build())
    }

}