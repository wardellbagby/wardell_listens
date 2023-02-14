package com.wardellbagby.listens.listenbrainz

import com.wardellbagby.listens.Configuration
import com.wardellbagby.listens.FakeLogger
import com.wardellbagby.listens.assertNotBlankOrNull
import com.wardellbagby.listens.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.Headers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ListenBrainzRepositoryTest {
  private val repository = ListenBrainzRepository(
    httpClient = createHttpClient(engine = FAKE_ENGINE),
    configuration = Configuration(
      listenbrainzUsername = "wardellbagby",
      ignoredTracksPath = Path("/tmp/ignored.txt"),
      dryRun = false,
      relativeStartInDays = 30
    ),
    logger = FakeLogger()
  )

  @Test
  fun `sends in correct timestamps`() = runBlocking {
    val expected = LISTENS.take(4)

    val actual = repository.fetchListens(
      start = LAST_LISTEN_TIME - 15.minutes,
      end = LAST_LISTEN_TIME
    )

    assertEquals(expected, actual)
  }

  @Test
  fun `does multiple calls when requesting more than 100`() = runBlocking {
    val expected = LISTENS.take(150)

    val actual = repository.fetchListens(
      start = LAST_LISTEN_TIME - 745.minutes,
      end = LAST_LISTEN_TIME
    )

    assertEquals(expected, actual)
  }

  private companion object {
    private val LAST_LISTEN_TIME = Instant.parse("2011-01-30T12:00:00-00:00")
    private val LISTENS = (0 until 1000).map {
      Listen(
        listened_at = (LAST_LISTEN_TIME - ((5 * it).minutes)).epochSeconds,
        track_metadata = TrackMetadata(
          artist_name = it.toString(),
          release_name = "A Whole Bunch Of Days Wonder",
          track_name = "Day $it",
          additional_info = null
        )
      )
    }

    private val FAKE_ENGINE = MockEngine { request ->
      val maxTimestamp =
        Instant.fromEpochSeconds(request.url.parameters["max_ts"].assertNotBlankOrNull().toLong())
      val count = request.url.parameters["count"].assertNotBlankOrNull().toInt()
      assertEquals(request.headers["Accept"], "application/json")

      respond(
        headers = Headers.build { append("Content-type", "application/json") },
        content = buildJsonObject {
          put("payload", buildJsonObject {
            put("listens", buildJsonArray {
              LISTENS
                .dropWhile { Instant.fromEpochSeconds(it.listened_at) > maxTimestamp }
                .take(count)
                .forEach {
                  add(Json.encodeToJsonElement(serializer = Listen.serializer(), value = it))
                }
            })
          })
        }.toString()
      )
    }
  }
}