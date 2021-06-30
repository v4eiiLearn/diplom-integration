package ru.vgtu.diplom.integration.handler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.slf4j.MDCContext
import org.camunda.bpm.client.task.ExternalTaskHandler
import org.slf4j.MDC
import org.springframework.stereotype.Component
import ru.vgtu.diplom.common.extensions.HOSTNAME
import ru.vgtu.diplom.common.logging.LoggingHeaderName
import ru.vgtu.diplom.common.logging.mdc.MdcService
import javax.annotation.PostConstruct

@Component
class DocumentValidatorHandler : OrchestratorTaskHandler() {

    @PostConstruct
    override fun handle() {
        val externalTaskHandler = ExternalTaskHandler { externalTask, externalTaskService ->
            logger.info("Catch documentValidate task. DocumentValid. ${externalTask.activityInstanceId}")
            MdcService.initMdcFromRequest(
                "local",
                "handle",
                mapOf(
                    Pair(LoggingHeaderName.CLIENT_ID.toLowerCase(), listOf(externalTask.getVariable("clientId") as String)),
                    Pair(LoggingHeaderName.INITIATOR_SERVICE.toLowerCase(), listOf(HOSTNAME))
                )
            )
            GlobalScope.launch(MDCContext(MDC.getCopyOfContextMap())) {
                val profile = applicationClient.getSuspendProfile(externalTask.businessKey)
                val passport = profile.documents?.find { it.docType == "PASSPORT" }
                    ?: throw Exception("Not found passport")
                val isPassportValid =
                    mockClient.sendPassportToValidate(passport.series + passport.number, externalTask.businessKey)
                        .awaitFirstOrDefault(false)

                delay(1000)

                externalTaskService.complete(externalTask, mapOf(Pair("isPassportValid", isPassportValid)))
            }
        }
        initHandler("documentValidate", externalTaskHandler)
    }
}