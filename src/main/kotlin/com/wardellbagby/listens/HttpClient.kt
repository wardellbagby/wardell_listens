package com.wardellbagby.listens

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.json.Json

fun createHttpClient(engine: HttpClientEngine): HttpClient {
  return HttpClient(engine = engine) {
    install(JsonFeature) {
      serializer = KotlinxSerializer(json = Json { ignoreUnknownKeys = true })
    }
  }
}