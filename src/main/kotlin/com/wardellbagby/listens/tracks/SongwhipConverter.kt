package com.wardellbagby.listens.tracks

import com.wardellbagby.listens.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single
class SongwhipConverter(
  private val httpClient: HttpClient,
  private val logger: Logger
) {
  suspend fun convert(url: String): String {
    logger.info("Converting URL \"$url\" to a Songwhip URL")
    return runCatching {
      httpClient.post {
        url(SONGWHIP_ENDPOINT)
        contentType(ContentType.Application.Json)
        setBody(mapOf("url" to url))
      }
        .body<SongwhipResponse>()
        .url
        .also {
          logger.info("Received Songwhip URL: $it")
        }
    }
      .onFailure {
        logger.warn("Failed to convert; falling back to Spotify URL", it)
      }
      .getOrDefault(url)
  }

  private companion object {
    private const val SONGWHIP_ENDPOINT = "https://songwhip.com/"
  }

  @Serializable
  private data class SongwhipResponse(
    val url: String
  )
}