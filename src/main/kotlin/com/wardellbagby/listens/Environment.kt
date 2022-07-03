package com.wardellbagby.listens

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Loads environment variables from a file and returns them as a map.
 *
 * If the file doesn't exist, returns an empty map.
 */
private fun loadFromEnvFile(filename: String): Map<String, String> {
  return loadFromResources(filename)
    ?.split("\n")
    ?.associate { line ->
      val (key, value) = line.split("=")
      key to value
    } ?: emptyMap()
}

/**
 * Represents environment variables that are needed in order to integrate with ListenBrainz
 * and Twitter.
 */
data class Environment(
  val listenbrainzUsername: String,
  val twitterConsumerKey: String,
  val twitterConsumerSecret: String,
  val twitterAccessToken: String,
  val twitterAccessTokenSecret: String,
  val ignoredTracksPath: Path
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
        ignoredTracksPath = Paths
          .get(it.getOrThrow("IGNORED_TRACKS_FILE"))
          .toAbsolutePath()
      )
    }