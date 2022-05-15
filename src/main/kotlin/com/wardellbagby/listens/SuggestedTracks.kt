package com.wardellbagby.listens

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The delay that should be used when accessing the ListenBrainz API.
 */
private val API_DELAY: Duration = 5.seconds

/**
 * The API endpoint for ListenBrainz that will return listens for a user.
 */
private val listensEndpoint =
  "https://api.listenbrainz.org/1/user/${environment.listenbrainzUsername}/listens"

@Serializable
data class Response(val payload: Payload)

@Serializable
data class Payload(val listens: List<Listen>)

@Serializable
data class AdditionalInfo(val spotify_id: String?)

@Serializable
data class Listen(val listened_at: Long, val track_metadata: TrackMetadata)

@Serializable
data class TrackMetadata(
  val additional_info: AdditionalInfo?,
  val artist_name: String,
  val release_name: String,
  val track_name: String,
)

data class SuggestedTrack(
  val name: String,
  val artist: String,
  val spotifyUrl: String,
  val listenCount: Int
)

/**
 * Get a track that is suitable to be suggested to people that hasn't ever been suggested before.
 *
 * @param ignoredSpotifyUrls Spotify URLs (gotten from [SuggestedTrack.spotifyUrl] that have been
 * previously suggested.
 * @param listens A list of [Listen]s from ListenBrainz.
 */
fun getSuggestedTrack(
  ignoredSpotifyUrls: List<String>,
  listens: List<Listen>
): SuggestedTrack {
  val trackCounts = listens
    .filter {
      // Filter out any listens that don't have a Spotify ID or that have been suggested before.
      val spotifyUrl = it.track_metadata.additional_info?.spotify_id
      spotifyUrl != null && spotifyUrl !in ignoredSpotifyUrls
    }
    .also {
      // Verifies that all Listens occur at distinctly different times to avoid potential
      // duplicates.
      val distinctByListenTime = it.distinctBy { listen -> listen.listened_at }

      require(distinctByListenTime.size == it.size) {
        "Received listens that have the exact same timestamp!"
      }
    }
    .groupBy {
      // Group all listens that share a Spotify ID
      it.track_metadata.additional_info!!.spotify_id
    }
    .map { (_, listens) ->
      // Convert into a Map of Listen to the number of times that Listen was seen.
      listens.first() to listens.size
    }
    .toMap()

  val (listen, count) = trackCounts.maxByOrNull { it.value }!!.let { it.key to it.value }

  return SuggestedTrack(
    name = listen.track_metadata.track_name,
    artist = listen.track_metadata.artist_name,
    spotifyUrl = listen.track_metadata.additional_info!!.spotify_id!!,
    listenCount = count
  )
}

/**
 * Fetch listens from ListenBrainz that occur between [start] and [end].
 */
suspend fun fetchListens(
  start: Instant,
  end: Instant
): List<Listen> {
  val startTimestamp = start.epochSeconds
  var endTimestamp = end.epochSeconds

  val expectedCount = 100
  var lastItemCount: Int
  val listens = mutableListOf<Listen>()

  do {
    val response = httpClient.get<Response>(listensEndpoint) {
      // This endpoint also accepts a "min_ts", but you can't specify both.
      parameter("max_ts", endTimestamp)
      parameter("count", expectedCount)
      header("Accept", "application/json")
    }

    val newListens = response.payload.listens.filter { it.listened_at >= startTimestamp }

    lastItemCount = newListens.size
    listens.addAll(newListens)

    println("Found $lastItemCount listens")

    if (lastItemCount == expectedCount) {
      // Subtract one to avoid duplicate listens.
      endTimestamp = newListens.last().listened_at - 1L
      delay(API_DELAY)
      println("Waiting to load more listens")
    } else {
      println("Loaded all listens!")
    }
  } while (lastItemCount == expectedCount)

  return listens
}