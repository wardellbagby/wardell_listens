package com.wardellbagby.listens.targets

import com.wardellbagby.listens.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import org.koin.core.annotation.Single

data class MicropubAuthentication(
  val endpoint: String,
  val accessToken: String,
)

@Single
class Micropub(
  private val auth: MicropubAuthentication,
  private val httpClient: HttpClient,
  private val logger: Logger
) : Target {
  override val loggableName: String = "Micropub"
  override val maxLength: Int = 500

  /**
   * Create a new post for the Micropub site specified by [endpoint] with the text contained
   * in [message].
   */
  override suspend fun post(message: String) {
    httpClient.submitForm<Unit>(
      url = auth.endpoint,
      formParameters = Parameters.build {
        append("h", "entry")
        append("content", message)
        append("access_token", auth.accessToken)
      }
    )
    logger.info("Successfully posted to Micropub!")
  }
}