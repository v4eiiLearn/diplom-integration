package ru.vgtu.diplom.integration.handler

import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.task.ExternalTaskHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import ru.vgtu.diplom.app.client.ApplicationClient
import ru.vgtu.diplom.common.logging.Loggable
import ru.vgtu.diplom.integration.client.MockClient
import ru.vgtu.diplom.integration.config.OrchestratorProperty

@Component
@EnableConfigurationProperties(OrchestratorProperty::class)
abstract class OrchestratorTaskHandler {
    companion object : Loggable

    @Autowired
    private lateinit var orchestratorProperty: OrchestratorProperty

    @Autowired
    lateinit var mockClient: MockClient

    @Autowired
    lateinit var applicationClient: ApplicationClient

    private var externalTaskClient: ExternalTaskClient? = null

    fun initHandler(topic: String, handler: ExternalTaskHandler) {
        if (externalTaskClient == null) {
            initExternalTaskClient()
        }
        externalTaskClient?.subscribe(topic)?.handler(handler)?.open()
        logger.info("Create subscriber for $topic")
    }

    private fun initExternalTaskClient() {
        with(orchestratorProperty) {
            externalTaskClient = ExternalTaskClient.create()
                .asyncResponseTimeout(asyncResponseTimeout.toMillis())
                .baseUrl(baseUrl)
                .lockDuration(lockDuration.toMillis())
                .build()
        }
    }

    abstract fun handle()
}