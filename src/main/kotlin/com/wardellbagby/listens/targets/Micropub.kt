package com.wardellbagby.listens.targets

import com.wardellbagby.listens.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
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
   * Create a new post for the Micropub site specified by [auth] with the text contained
   * in [message].
   */
  override suspend fun post(message: String) {
    val response = httpClient.submitForm(
      url = auth.endpoint,
      formParameters = Parameters.build {
        append("h", "entry")
        append("content", message)
        append("access_token", auth.accessToken)
      }
    )

    val body = response.body<Map<String, String>>()

    if (response.status.isSuccess()) {
      if (!body["error"].isNullOrBlank()) {
        val errorMessage = body["error"]!!
        val errorDescription = body["error_description"] ?: ""

        error("$errorMessage $errorDescription")
      } else {
        logger.info("Successfully posted to Micropub!")
      }
    } else {
      error("Failed to post to Micropub. HTTP Code: ${response.status.value} Details: ${body["detail"]}")
    }
  }
}