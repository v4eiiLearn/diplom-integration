package ru.vgtu.diplom.integration.handler

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import org.camunda.bpm.client.task.ExternalTaskHandler
import org.slf4j.MDC
import org.springframework.stereotype.Component
import ru.vgtu.diplom.app.client.ApplicationClient
import ru.vgtu.diplom.common.extensions.HOSTNAME
import ru.vgtu.diplom.common.logging.LoggingHeaderName
import ru.vgtu.diplom.common.logging.mdc.MdcService
import ru.vgtu.diplom.integration.client.MockClient
import javax.annotation.PostConstruct

@Component
class IncomeValidatorHandler : OrchestratorTaskHandler() {

    @PostConstruct
    override fun handle() {
        val externalTaskHandler = ExternalTaskHandler { externalTask, externalTaskService ->
            logger.info("Catch income validate task. IncomeValid. ${externalTask.activityInstanceId}")
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
                val incomes = mockClient.sendDataToFns(profile.clientId.orEmpty(), profile.documents.orEmpty())

                externalTaskService.complete(
                    externalTask,
                    mapOf(Pair("isIncomeValid", profile.incomes.orEmpty() == incomes))
                )
            }
        }
        initHandler("incomeValidate", externalTaskHandler)
    }


}