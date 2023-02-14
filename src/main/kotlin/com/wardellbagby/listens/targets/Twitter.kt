package com.wardellbagby.listens.targets

import blue.starry.jsonkt.asJsonElement
import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.request.jsonBody
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.token
import blue.starry.penicillin.core.session.post
import blue.starry.penicillin.models.Status
import com.wardellbagby.listens.Logger
import io.ktor.utils.io.core.use
import kotlinx.serialization.json.buildJsonObject
import org.koin.core.annotation.Single

data class TwitterAuthentication(
  val consumerKey: String,
  val consumerSecret: String,
  val accessToken: String,
  val accessTokenSecret: String,
)

@Single
class Twitter(
  private val auth: TwitterAuthentication,
  private val logger: Logger
) : Target {
  override val loggableName: String = "Twitter"
  override val maxLength: Int = 280

  /**
   * Post a Tweet to the account specified by [auth] with the text contained
   * in [message].
   */
  override suspend fun post(message: String) = runCatching {
    // We expect the Twitter API to stop working eventually so just ignore failures here.
    PenicillinClient {
      account {
        application(
          consumerKey = auth.consumerKey,
          consumerSecret = auth.consumerSecret
        )
        token(
          accessToken = auth.accessToken,
          accessTokenSecret = auth.accessTokenSecret
        )
      }
    }.use { client ->
      client.session.post("/2/tweets") {
        jsonBody(json = buildJsonObject {
          put("text", message.asJsonElement())
        })
      }
        .jsonObject { Status(it, client) }
        .execute()
    }
  }
    .fold(onFailure = {
      logger.info("Failed to post to Twitter")
    }, onSuccess = {
      logger.info("Successfully posted to Twitter")
    })
}
