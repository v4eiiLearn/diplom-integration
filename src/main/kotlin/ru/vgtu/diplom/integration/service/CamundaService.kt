package ru.vgtu.diplom.integration.service

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto
import org.camunda.bpm.engine.variable.Variables
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.vgtu.diplom.common.logging.Loggable
import ru.vgtu.diplom.integration.config.OrchestratorProperty
import ru.vgtu.diplom.integration.dto.Message.*

@Component
@EnableConfigurationProperties(OrchestratorProperty::class)
class CamundaService(
    private val webClient: WebClient,
    private val orchestratorProperty: OrchestratorProperty
) {
    companion object : Loggable {
        private const val MESSAGE_ENDPOINT = "/message"
    }

    suspend fun sendClientValidateMessage(businessKey: String, variables: Map<String, Any>?) {
        sendCorrelateMessage(CLIENT_VALIDATE_MESSAGE.value, businessKey, variables)
    }

    private suspend fun sendCorrelateMessage(msgName: String, businessKey: String, variables: Map<String, Any>?) {
        try {
            val messageDto = CorrelationMessageDto()
            messageDto.messageName = msgName
            messageDto.businessKey = businessKey
            messageDto.correlationKeys = mapOf()
            messageDto.localCorrelationKeys = mapOf()
            messageDto.processVariables = VariableValueDto.fromMap(Variables.fromMap(variables))
            messageDto.processVariablesLocal = mapOf()
            webClient.post()
                .uri(orchestratorProperty.baseUrl + MESSAGE_ENDPOINT)
                .body(Mono.just(messageDto), CorrelationMessageDto::class.java)
                .retrieve()
                .bodyToMono(Unit::class.java)
                .awaitFirstOrNull()
        } catch (e: RestClientException) {
            logger.error("Error send correlate message. Exception message: " + e.message)
            throw Exception(e.message + " Business key: " + businessKey)
        }
    }

    suspend fun sendSolvencyMessage(businessKey: String, variables: Map<String, Any>?) {
        sendCorrelateMessage(CLIENT_SOLVENCY_MESSAGE.value, businessKey, variables)
    }

    suspend fun sendDecisionMessage(businessKey: String, variables: Map<String, Any>?) {
        sendCorrelateMessage(CLIENT_DECISION_MESSAGE.value, businessKey, variables)
    }

    suspend fun getProcessVariables(businessKey: String): Map<String, VariableValueDto> {
        try {
            val processInstances = webClient.get()
                .uri("${orchestratorProperty.baseUrl}/process-instance?businessKey=$businessKey")
                .retrieve()
                .bodyToMono(Array<ProcessInstanceDto?>::class.java)
                .awaitFirstOrNull()
            if (processInstances != null && processInstances[0] != null) {
                return webClient.get()
                    .uri("${orchestratorProperty.baseUrl}/process-instance/${processInstances[0]?.id}/variables")
                    .retrieve()
                    .bodyToMono(object : ParameterizedTypeReference<Map<String, VariableValueDto>>() {})
                    .awaitFirst()
            }
        } catch (e: Exception) {
            logger.error("Error getting process variables, businessKey: $businessKey, error: ${e.message}")
        }
        return emptyMap()
    }

}