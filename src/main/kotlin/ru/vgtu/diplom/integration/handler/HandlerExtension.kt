package ru.vgtu.diplom.integration.handler

import kotlinx.coroutines.reactive.awaitFirstOrNull
import ru.vgtu.diplom.app.client.ApplicationClient


suspend fun ApplicationClient.getSuspendProfile(appId: String) =
    this.getProfile(appId).awaitFirstOrNull() ?: throw Exception("Not found profile")

suspend fun ApplicationClient.getSuspendApplication(appId: String) =
    this.getApplicationByAppId(appId).awaitFirstOrNull() ?: throw Exception("Not found application")