package com.wardellbagby.listens

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.json.Json

val httpClient = HttpClient(CIO) {
  install(JsonFeature) {
    serializer = KotlinxSerializer(json = Json { ignoreUnknownKeys = true })
  }
}