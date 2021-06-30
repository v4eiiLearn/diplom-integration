package ru.vgtu.diplom.integration.handler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.slf4j.MDCContext
import org.camunda.bpm.client.task.ExternalTaskHandler
import org.slf4j.MDC
import org.springframework.stereotype.Component
import ru.vgtu.diplom.common.context.core.resolver.WebMvcMsaContextResolver
import ru.vgtu.diplom.common.extensions.HOSTNAME
import ru.vgtu.diplom.common.logging.LoggingHeaderName
import ru.vgtu.diplom.common.logging.mdc.MdcService
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Component
class BkiHandler(
    private val webMvcMsaContextResolver: WebMvcMsaContextResolver
) : OrchestratorTaskHandler() {

    companion object {
        const val flag = "isBki"
    }

    @PostConstruct
    override fun handle() {
        val externalTaskHandler = ExternalTaskHandler { externalTask, externalTaskService ->
            MdcService.initMdcFromRequest(
                "local",
                "handle",
                mapOf(
                    Pair(LoggingHeaderName.CLIENT_ID.toLowerCase(), listOf(externalTask.getVariable("clientId") as String)),
                    Pair(LoggingHeaderName.INITIATOR_SERVICE.toLowerCase(), listOf(HOSTNAME))
                )
            )
            logger.info("Catch bki task. ${externalTask.activityInstanceId}, businessKey: ${externalTask.businessKey}")
            GlobalScope.launch(MDCContext(MDC.getCopyOfContextMap())) {
                val profile = applicationClient.getSuspendProfile(externalTask.businessKey)
                val passport = profile.documents?.find { it.docType == "PASSPORT" }
                    ?: throw Exception("Passport not found")
                val creditHistory = mockClient.sendDataToBki(profile.clientId.orEmpty(), passport).awaitFirstOrNull()
                    ?: throw Exception("Not found creditHistory")
                val lastDateIssue = creditHistory.creditList.maxByOrNull { it.dateIssue }?.dateIssue
                    ?: throw Exception("Null dateIssue")
                val variableMap =
                    if (creditHistory.creditList.isNotEmpty() && lastDateIssue < LocalDateTime.now().plusDays(30))
                        mapOf(Pair(flag, true))
                    else
                        mapOf(Pair(flag, true))

                externalTaskService.complete(externalTask, variableMap)
            }
        }
        initHandler("bkiValidate", externalTaskHandler)
    }
}