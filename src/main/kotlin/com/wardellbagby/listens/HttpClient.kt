package com.wardellbagby.listens

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*

fun createHttpClient(engine: HttpClientEngine): HttpClient {
  return HttpClient(engine = engine) {
    install(ContentNegotiation) {
      json(json = Json { ignoreUnknownKeys = true })
    }
  }
}