package com.wardellbagby.listens.listenbrainz

import com.wardellbagby.listens.Configuration
import com.wardellbagby.listens.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaZoneId
import org.koin.core.annotation.Single
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.FULL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Single
class ListenBrainzRepository(
  private val httpClient: HttpClient,
  configuration: Configuration,
  private val logger: Logger
) {
  /**
   * Fetch listens from ListenBrainz that occur between [start] and [end].
   */
  suspend fun fetchListens(
    start: Instant,
    end: Instant
  ): List<Listen> {
    val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FULL)
    val zoneId = TimeZone.currentSystemDefault().toJavaZoneId()

    val startDateTime = ZonedDateTime.ofInstant(start.toJavaInstant(), zoneId)
    val endDateTime = ZonedDateTime.ofInstant(end.toJavaInstant(), zoneId)

    logger.info(
      "Fetching listens from ${dateTimeFormatter.format(startDateTime)} to ${dateTimeFormatter.format(endDateTime)}"
    )

    val startTimestamp = start.epochSeconds
    var endTimestamp = end.epochSeconds

    val expectedCount = 100
    var lastItemCount: Int
    val listens = mutableListOf<Listen>()

    do {
      with(ZonedDateTime.ofInstant(Instant.fromEpochSeconds(endTimestamp).toJavaInstant(), zoneId)) {
        logger.verbose(
          "Fetching at most $expectedCount listens that occurred before ${dateTimeFormatter.format(this)} (epoch seconds: $endTimestamp)"
        )
      }

      val response = httpClient.get {
        url(listensEndpoint)
        // This endpoint also accepts a "min_ts", but you can't specify both.
        parameter("max_ts", endTimestamp)
        parameter("count", expectedCount)
        header("Accept", "application/json")
      }
        .body<ListensResponse>()

      val newListens = response.payload.listens.filter { it.listened_at >= startTimestamp }

      lastItemCount = newListens.size
      listens.addAll(newListens)

      logger.verbose("Found $lastItemCount listens")

      if (lastItemCount == expectedCount) {
        // Subtract one to avoid duplicate listens.
        endTimestamp = newListens.last().listened_at - 1L
        logger.verbose("Waiting to load more listens")
        delay(API_DELAY)
      } else {
        logger.info("Loaded all listens!")
      }
    } while (lastItemCount == expectedCount)

    return listens
  }

  /**
   * The API endpoint for ListenBrainz that will return listens for a user.
   */
  private val listensEndpoint =
    "https://api.listenbrainz.org/1/user/${configuration.listenbrainzUsername}/listens"

  private companion object {
    /**
     * The delay that should be used when accessing the ListenBrainz API.
     */
    private val API_DELAY: Duration = 5.seconds
  }
}