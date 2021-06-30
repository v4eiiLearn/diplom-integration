package ru.vgtu.diplom.integration.handler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import org.camunda.bpm.client.task.ExternalTaskHandler
import org.slf4j.MDC
import org.springframework.stereotype.Component
import ru.vgtu.diplom.common.extensions.HOSTNAME
import ru.vgtu.diplom.common.logging.LoggingHeaderName
import ru.vgtu.diplom.common.logging.mdc.MdcService
import javax.annotation.PostConstruct

@Component
class DecisionHandler : OrchestratorTaskHandler() {

    @PostConstruct
    override fun handle() {
        val handler = ExternalTaskHandler { externalTask, externalTaskService ->
            logger.info("Catch decision task ${externalTask.activityInstanceId}, businessKey: ${externalTask.businessKey}")
            MdcService.initMdcFromRequest(
                "local",
                "handle",
                mapOf(
                    Pair(LoggingHeaderName.CLIENT_ID.toLowerCase(), listOf(externalTask.getVariable("clientId") as String)),
                    Pair(LoggingHeaderName.INITIATOR_SERVICE.toLowerCase(), listOf(HOSTNAME))
                )
            )
            GlobalScope.launch(MDCContext(MDC.getCopyOfContextMap())) {
                val application = applicationClient.getSuspendApplication(externalTask.businessKey)
                mockClient.sendToDecisionManager(application).subscribe()

                externalTaskService.complete(externalTask)
            }
        }
        initHandler("makeDecision", handler)
    }
}