package com.wardellbagby.listens

import com.wardellbagby.listens.Target.Micropub
import com.wardellbagby.listens.Target.Twitter
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

sealed interface Target {
  data class Twitter(
    val consumerKey: String,
    val consumerSecret: String,
    val accessToken: String,
    val accessTokenSecret: String,
  ) : Target

  data class Micropub(
    val endpoint: String,
    val accessToken: String
  ) : Target
}

/**
 * Represents environment variables that are needed in order to integrate with ListenBrainz
 * and Twitter.
 */
data class Environment(
  val listenbrainzUsername: String,
  val ignoredTracksPath: Path,
  val targets: List<Target>
)

private fun Map<String, String>.getOrThrow(key: String): String {
  return get(key) ?: error("Expected environment variable with key \"$key\" to have been set!")
}

private fun Map<String, String>.isTwitterEnv(): Boolean {
  return containsKeys(
    TWITTER_CONSUMER_KEY,
    TWITTER_CONSUMER_SECRET,
    TWITTER_ACCESS_TOKEN,
    TWITTER_ACCESS_TOKEN_SECRET
  )
}

private fun Map<String, String>.isMicropubEnv(): Boolean {
  return containsKeys(MICROPUB_ACCESS_TOKEN, MICROPUB_ENDPOINT)
}

private fun <T> Map<T, *>.containsKeys(vararg keys: T): Boolean {
  return keys.all { contains(it) }
}

private const val secretsEnvFile = "secrets.env"
val environment: Environment =
  (System.getenv() + loadFromEnvFile(secretsEnvFile))
    .let {
      Environment(
        listenbrainzUsername = it.getOrThrow("LISTENBRAINZ_USERNAME"),
        ignoredTracksPath = Paths
          .get(it.getOrThrow("IGNORED_TRACKS_FILE"))
          .toAbsolutePath(),
        targets = buildList {
          if (it.isTwitterEnv()) {
            add(it.createTwitterTarget())
          }
          if (it.isMicropubEnv()) {
            add(it.createMicropubTarget())
          }
        }.ifEmpty {
          error("Not enough environment variables to publish to either Twitter or Micropup")
        }
      )
    }

private fun Map<String, String>.createTwitterTarget(): Twitter {
  return Twitter(
    accessToken = getOrThrow(TWITTER_ACCESS_TOKEN),
    accessTokenSecret = getOrThrow(TWITTER_ACCESS_TOKEN_SECRET),
    consumerKey = getOrThrow(TWITTER_CONSUMER_KEY),
    consumerSecret = getOrThrow(TWITTER_CONSUMER_SECRET)
  )
}

private fun Map<String, String>.createMicropubTarget(): Micropub {
  return Micropub(
    endpoint = getOrThrow(MICROPUB_ENDPOINT),
    accessToken = getOrThrow(MICROPUB_ACCESS_TOKEN)
  )
}

private const val MICROPUB_ACCESS_TOKEN = "MICROPUB_ACCESS_TOKEN"
private const val MICROPUB_ENDPOINT = "MICROPUB_ENDPOINT"
private const val TWITTER_CONSUMER_KEY = "TWITTER_CONSUMER_KEY"
private const val TWITTER_CONSUMER_SECRET = "TWITTER_CONSUMER_SECRET"
private const val TWITTER_ACCESS_TOKEN = "TWITTER_ACCESS_TOKEN"
private const val TWITTER_ACCESS_TOKEN_SECRET = "TWITTER_ACCESS_TOKEN_SECRET"