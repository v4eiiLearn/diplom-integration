package ru.vgtu.diplom.integration.client

import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ru.vgtu.diplom.app.model.*
import ru.vgtu.diplom.integration.dto.CreditHistory

class MockClient(
    private val webClient: WebClient
) {
    fun sendDataToBki(clientId: String, profileDocument: Document) =
        webClient.post()
            .uri("/integration/$clientId/bki")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(profileDocument), Document::class.java)
            .retrieve()
            .bodyToMono(CreditHistory::class.java)

    fun sendPassportToValidate(passport: String, appId: String) =
        webClient.post()
            .uri("/integration/$appId/passport")
            .body(Mono.just(passport), String::class.java)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Boolean::class.java)

    fun sendDataToFns(clientId: String, profileDocuments: List<Document>) =
        webClient.post()
            .uri("/integration/$clientId/fns")
            .body(Mono.just(profileDocuments), List::class.java)
            .retrieve()
            .bodyToMono(Income::class.java)

    fun sendDataToAnalysis(clientId: String, appId: String, condition: Condition) =
        webClient.post()
            .uri("/manager/$clientId/$appId/analysis")
            .body(Mono.just(condition), Condition::class.java)
            .retrieve()
            .toBodilessEntity()

    fun sendToDecisionManager(application: Application) =
        webClient.post()
            .uri("/integration/decision")
            .body(Mono.just(application), Application::class.java)
            .retrieve()
            .bodyToMono(Unit::class.java)



}