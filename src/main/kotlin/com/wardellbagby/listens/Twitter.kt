package com.wardellbagby.listens

import blue.starry.jsonkt.asJsonElement
import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.request.jsonBody
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.token
import blue.starry.penicillin.core.session.post
import blue.starry.penicillin.models.Status
import io.ktor.utils.io.core.use
import kotlinx.serialization.json.buildJsonObject

/**
 * Post a Tweet to the account specified by [Environment.twitterAccessToken] with the text contained
 * in [message].
 */
suspend fun postTweet(message: String) {
  PenicillinClient {
    account {
      application(
        consumerKey = environment.twitterConsumerKey,
        consumerSecret = environment.twitterConsumerSecret
      )
      token(
        accessToken = environment.twitterAccessToken,
        accessTokenSecret = environment.twitterAccessTokenSecret
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