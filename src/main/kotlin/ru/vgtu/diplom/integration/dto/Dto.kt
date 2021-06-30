package ru.vgtu.diplom.integration.dto

import java.math.BigDecimal
import java.time.LocalDateTime


enum class Message(val value: String) {
    CLIENT_VALIDATE_MESSAGE("ClientValidateMessage"),
    CLIENT_SOLVENCY_MESSAGE("SolvencyMessage"),
    CLIENT_DECISION_MESSAGE("DecisionMessage")
}

data class ClientValidation(
    val appId: String,
    val isValid: Boolean
)

data class CreditHistory(
    val personId: String,
    val creditList: List<Credit>
)

data class Credit(
    var id: Long,
    var price: BigDecimal,
    var percent: Float,
    var term: Long,
    var type: String,
    var dateIssue: LocalDateTime
)