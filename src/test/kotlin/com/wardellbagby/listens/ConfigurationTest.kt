package com.wardellbagby.listens

import com.wardellbagby.listens.targets.MicropubAuthentication
import com.wardellbagby.listens.targets.TwitterAuthentication
import org.junit.Test
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.assertEquals

internal class ConfigurationTest {

  @Test
  fun `sets dry run to false when running on CI`() {
    val env = MIN_ENV + mapOf("CI" to "true")
    val expected = minConfiguration(
      dryRun = false,
    )

    val actual = env.createConfigurationFromEnv()

    assertEquals(expected, actual)
  }

  @Test
  fun `set dry run to true when not running on CI`() {
    val env = MIN_ENV
    val expected = minConfiguration(
      dryRun = true,
    )

    val actual = env.createConfigurationFromEnv()

    assertEquals(expected, actual)
  }

  @Test
  fun `respects dry run when explicitly set to true`() {
    val env = MIN_ENV + mapOf("DRY_RUN" to "true", "CI" to "true")
    val expected = minConfiguration(
      dryRun = true,
    )

    val actual = env.createConfigurationFromEnv()

    assertEquals(expected, actual)
  }

  @Test
  fun `respects dry run when explicitly set to false`() {
    val env = MIN_ENV + mapOf("DRY_RUN" to "false")
    val expected = minConfiguration(
      dryRun = false,
    )

    val actual = env.createConfigurationFromEnv()

    assertEquals(expected, actual)
  }

  @Test
  fun `creates twitter auth when all twitter fields are set`() {
    val twitterAuthentication = TwitterAuthentication(
      consumerKey = "hello",
      consumerSecret = "bots",
      accessToken = "im",
      accessTokenSecret = "wardell"
    )
    val env = MIN_ENV + mapOf(
      "TWITTER_CONSUMER_KEY" to twitterAuthentication.consumerKey,
      "TWITTER_CONSUMER_SECRET" to twitterAuthentication.consumerSecret,
      "TWITTER_ACCESS_TOKEN" to twitterAuthentication.accessToken,
      "TWITTER_ACCESS_TOKEN_SECRET" to twitterAuthentication.accessTokenSecret
    )

    val expected = minConfiguration(
      twitterAuthentication = twitterAuthentication
    )

    val actual = env.createConfigurationFromEnv()

    assertEquals(expected, actual)
  }

  @Test
  fun `creates twitter auth when some twitter fields are not set`() {
    val env = MIN_ENV + mapOf(
      "TWITTER_CONSUMER_KEY" to "1",
      "TWITTER_CONSUMER_SECRET" to "2",
    )

    val expected = minConfiguration(
      twitterAuthentication = null
    )

    val actual = env.createConfigurationFromEnv()

    assertEquals(expected, actual)
  }

  @Test
  fun `creates micropub auth when all micropub fields are set`() {
    val micropubAuthentication = MicropubAuthentication(
      endpoint = "https://ohwow.com",
      accessToken = "hehehe",
    )
    val env = MIN_ENV + mapOf(
      "MICROPUB_ENDPOINT" to micropubAuthentication.endpoint,
      "MICROPUB_ACCESS_TOKEN" to micropubAuthentication.accessToken
    )

    val expected = minConfiguration(
      micropubAuthentication = micropubAuthentication
    )

    val actual = env.createConfigurationFromEnv()

    assertEquals(expected, actual)
  }

  @Test
  fun `creates micropub auth when some micropub fields are not set`() {
    val env = MIN_ENV + mapOf(
      "MICROPUB_ENDPOINT" to "1",
    )

    val expected = minConfiguration(
      micropubAuthentication = null
    )

    val actual = env.createConfigurationFromEnv()

    assertEquals(expected, actual)
  }

  private companion object {
    val MIN_ENV = mapOf(
      "LISTENBRAINZ_USERNAME" to "wardell",
      "IGNORED_TRACKS_FILE" to "/tmp/ignored.txt"
    )

    fun minConfiguration(
      listenbrainzUsername: String = "wardell",
      ignoredTracksPath: Path = Path("/tmp/ignored.txt"),
      dryRun: Boolean = true,
      twitterAuthentication: TwitterAuthentication? = null,
      micropubAuthentication: MicropubAuthentication? = null,
      relativeStartInDays: Int = 30
    ): Configuration {
      return Configuration(
        listenbrainzUsername = listenbrainzUsername,
        ignoredTracksPath = ignoredTracksPath,
        twitterAuthentication = twitterAuthentication,
        micropubAuthentication = micropubAuthentication,
        dryRun = dryRun,
        relativeStartInDays = relativeStartInDays
      )
    }
  }
}