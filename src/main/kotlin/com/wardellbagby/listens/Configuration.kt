package com.wardellbagby.listens

import com.wardellbagby.listens.targets.MicropubAuthentication
import com.wardellbagby.listens.targets.TwitterAuthentication
import com.wardellbagby.listens.telegram.TelegramAuthentication
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
    ?.filterNot { it.startsWith("#") || it.startsWith("//") }
    ?.associate { line ->
      val (key, value) = line.split("=")
      key to value
    } ?: emptyMap()
}

/**
 * Represents the configuration that is needed in order to integrate with ListenBrainz
 * and the postable targets.
 */
data class Configuration(
  val listenbrainzUsername: String,
  val ignoredTracksPath: Path,
  val twitterAuthentication: TwitterAuthentication? = null,
  val micropubAuthentication: MicropubAuthentication? = null,
  val dryRun: Boolean,
  val relativeStartInDays: Int,
  val manuallySelectTrack: Boolean = false,
  val telegramAuthentication: TelegramAuthentication? = null
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

fun getEnv(): Map<String, String> {
  return System.getenv() + loadFromEnvFile(secretsEnvFile)
}

fun Map<String, String>.createConfigurationFromEnv(): Configuration {
  val manuallySelectTrack = get("MANUALLY_SELECT_TRACK").toBoolean()
  return Configuration(
    listenbrainzUsername = getOrThrow("LISTENBRAINZ_USERNAME"),
    ignoredTracksPath = Paths
      .get(getOrThrow("IGNORED_TRACKS_FILE"))
      .toAbsolutePath(),
    twitterAuthentication = runIf(isTwitterEnv()) { createTwitterAuth() },
    micropubAuthentication = runIf(isMicropubEnv()) { createMicropubAuth() },
    dryRun = getDryRunValue(),
    relativeStartInDays = get("RELATIVE_START_IN_DAYS")?.toIntOrNull() ?: 30,
    manuallySelectTrack = manuallySelectTrack,
    telegramAuthentication = runIf(manuallySelectTrack) { createTelegramAuthentication() }
  )
}

private fun Map<String, String>.getDryRunValue(): Boolean {
  val dryRun = get("DRY_RUN")
  val isCI = get("CI") == "true"

  return when (dryRun) {
    "true" -> true
    null -> !isCI
    // If they've explicitly set dry-run to something that isn't true, assume it's false.
    else -> false
  }
}

private fun Map<String, String>.createTwitterAuth(): TwitterAuthentication {
  return TwitterAuthentication(
    accessToken = getOrThrow(TWITTER_ACCESS_TOKEN),
    accessTokenSecret = getOrThrow(TWITTER_ACCESS_TOKEN_SECRET),
    consumerKey = getOrThrow(TWITTER_CONSUMER_KEY),
    consumerSecret = getOrThrow(TWITTER_CONSUMER_SECRET)
  )
}

private fun Map<String, String>.createMicropubAuth(): MicropubAuthentication {
  return MicropubAuthentication(
    endpoint = getOrThrow(MICROPUB_ENDPOINT),
    accessToken = getOrThrow(MICROPUB_ACCESS_TOKEN)
  )
}

private fun Map<String, String>.createTelegramAuthentication(): TelegramAuthentication {
  return TelegramAuthentication(
    botToken = getOrThrow(TELEGRAM_BOT_TOKEN),
    chatId = getOrThrow(TELEGRAM_CHAT_ID).toLong()
  )
}

private const val MICROPUB_ACCESS_TOKEN = "MICROPUB_ACCESS_TOKEN"
private const val MICROPUB_ENDPOINT = "MICROPUB_ENDPOINT"
private const val TWITTER_CONSUMER_KEY = "TWITTER_CONSUMER_KEY"
private const val TWITTER_CONSUMER_SECRET = "TWITTER_CONSUMER_SECRET"
private const val TWITTER_ACCESS_TOKEN = "TWITTER_ACCESS_TOKEN"
private const val TWITTER_ACCESS_TOKEN_SECRET = "TWITTER_ACCESS_TOKEN_SECRET"
private const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"
private const val TELEGRAM_CHAT_ID = "TELEGRAM_CHAT_ID"

private fun <T> runIf(predicate: Boolean, block: () -> T): T? {
  return if (predicate) {
    block()
  } else {
    null
  }
}