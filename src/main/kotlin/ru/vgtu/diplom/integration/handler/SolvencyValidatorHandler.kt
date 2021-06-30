package ru.vgtu.diplom.integration.handler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.slf4j.MDCContext
import org.camunda.bpm.client.task.ExternalTaskHandler
import org.slf4j.MDC
import org.springframework.stereotype.Component
import ru.vgtu.diplom.common.extensions.HOSTNAME
import ru.vgtu.diplom.common.logging.LoggingHeaderName
import ru.vgtu.diplom.common.logging.mdc.MdcService
import javax.annotation.PostConstruct

@Component
class SolvencyValidatorHandler : OrchestratorTaskHandler() {

    @PostConstruct
    override fun handle() {
        val externalTaskHandler = ExternalTaskHandler { externalTask, externalTaskService ->
            logger.info("Catch solvency validate task. SolvencyValid. ${externalTask.activityInstanceId}")
            MdcService.initMdcFromRequest(
                "local",
                "handle",
                mapOf(
                    Pair(LoggingHeaderName.CLIENT_ID.toLowerCase(), listOf(externalTask.getVariable("clientId") as String)),
                    Pair(LoggingHeaderName.INITIATOR_SERVICE.toLowerCase(), listOf(HOSTNAME))
                )
            )
            GlobalScope.launch(MDCContext(MDC.getCopyOfContextMap())) {
                val clientId = applicationClient.getSuspendProfile(externalTask.businessKey).clientId.orEmpty()
                val condition = applicationClient.getCondition(clientId).awaitFirstOrNull()
                    ?: throw Exception("Not found condition")
                mockClient.sendDataToAnalysis(clientId, externalTask.businessKey, condition).subscribe()

                externalTaskService.complete(externalTask)
            }
        }
        initHandler("solvencyValidate", externalTaskHandler)
    }

}