package com.wardellbagby.listens

import java.nio.file.Path
import java.nio.file.Paths

private fun loadFromEnvFile(filename: String): Map<String, String> {
  return loadFromResources(filename)
    ?.split("\n")
    ?.associate { line ->
      val (key, value) = line.split("=")
      key to value
    }
    ?: emptyMap()
}

data class Environment(
  val listenbrainzUsername: String,
  val twitterConsumerKey: String,
  val twitterConsumerSecret: String,
  val twitterAccessToken: String,
  val twitterAccessTokenSecret: String,
  val ignoredTracksOutput: Path
)

private fun Map<String, String>.getOrThrow(key: String): String {
  return get(key) ?: error("Expected environment variable with key \"$key\" to have been set!")
}

private const val secretsEnvFile = "secrets.env"
val environment: Environment =
  (System.getenv() + loadFromEnvFile(secretsEnvFile))
    .let {
      Environment(
        listenbrainzUsername = it.getOrThrow("LISTENBRAINZ_USERNAME"),
        twitterConsumerKey = it.getOrThrow("TWITTER_CONSUMER_KEY"),
        twitterConsumerSecret = it.getOrThrow("TWITTER_CONSUMER_SECRET"),
        twitterAccessToken = it.getOrThrow("TWITTER_ACCESS_TOKEN"),
        twitterAccessTokenSecret = it.getOrThrow("TWITTER_ACCESS_TOKEN_SECRET"),
        ignoredTracksOutput = Paths.get(it.getOrThrow("IGNORED_TRACKS_OUTPUT"))
      )
    }