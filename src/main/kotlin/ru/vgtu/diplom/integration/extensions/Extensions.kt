package ru.vgtu.diplom.integration.extensions

import org.camunda.bpm.engine.rest.dto.VariableValueDto
import org.slf4j.MDC
import ru.vgtu.diplom.common.logging.mdc.MdcVariableName


fun Map<String, VariableValueDto>.fillMdc() {
    MDC.put(MdcVariableName.CLIENT_ID, this["clientId"]?.value as String)
}