package ru.vgtu.diplom.integration.controller

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ru.vgtu.diplom.app.client.ApplicationClient
import ru.vgtu.diplom.app.model.Decision
import ru.vgtu.diplom.integration.dto.ClientValidation
import ru.vgtu.diplom.integration.extensions.fillMdc
import ru.vgtu.diplom.integration.service.CamundaService


@RestController
class AsyncResponseController(
    private val camundaService: CamundaService,
    private val applicationClient: ApplicationClient
) {
    @PostMapping("/integration/diplom/client/valid")
    suspend fun clientValidateResponse(@RequestBody clientValidation: ClientValidation): ResponseEntity<Unit> {
        camundaService.getProcessVariables(clientValidation.appId).fillMdc()
        camundaService.sendClientValidateMessage(
            clientValidation.appId,
            mapOf(Pair("isClientValid", clientValidation.isValid))
        )
        return ResponseEntity.ok().build()
    }

    @PostMapping("/integration/diplom/client/solvency")
    suspend fun clientSolvency(@RequestBody clientValidation: ClientValidation): ResponseEntity<Unit> {
        camundaService.getProcessVariables(clientValidation.appId).fillMdc()
        camundaService.sendSolvencyMessage(clientValidation.appId, mapOf(Pair("isSolvency", clientValidation.isValid)))

        return ResponseEntity.ok().build()
    }

    @PostMapping("/integration/diplom/client/decision")
    suspend fun clientDecision(@RequestBody decision: Decision): ResponseEntity<Unit> {
        camundaService.getProcessVariables(decision.applicationId!!).fillMdc()
        val appId = decision.applicationId ?: throw Exception("decision applicationId is null")
        val status = decision.statusCode ?: throw Exception("decision status is null")

        val application = applicationClient.getApplicationByAppId(appId).awaitFirst()
        application.decision = decision
        applicationClient.updateApplication(application).subscribe()

        camundaService.sendDecisionMessage(appId, mapOf(Pair("decision", status)))

        return ResponseEntity.ok().build()
    }
}