package com.wardellbagby.listens.tracks

import com.wardellbagby.listens.Logger
import com.wardellbagby.listens.listenbrainz.Listen
import org.koin.core.annotation.Single
import kotlin.random.Random

data class SuggestedTrack(
  val id: String,
  val name: String,
  val artist: String,
  val url: String,
  val listenCount: Int
)

@Single
class TrackSuggester(
  private val ignoredTracks: IgnoredTracks,
  private val logger: Logger,
  private val random: Random
) {
  /**
   * Get a track that is suitable to be suggested to people that hasn't ever been suggested before.
   *
   * @param listens A list of [Listen]s from ListenBrainz.
   */
  fun generate(listens: List<Listen>): SuggestedTrack? {
    val trackCounts = listens
      .filter {
        // Filter out any listens that don't have a Spotify ID or that have been suggested before.
        val spotifyUrl = it.track_metadata?.additional_info?.spotify_id?.ifBlank { null }
        val artistName = it.track_metadata?.artist_name?.ifBlank { null }
        val trackName = it.track_metadata?.track_name?.ifBlank { null }

        val isIgnoredTrack = spotifyUrl in ignoredTracks
        val isListenSuggestible = spotifyUrl != null &&
                artistName != null &&
                trackName != null &&
                !isIgnoredTrack

        if (!isListenSuggestible) {
          val reason = when {
            spotifyUrl == null -> "without a Spotify ID"
            artistName == null -> "without an artist name"
            trackName == null -> "without a track name"
            else -> "that is ignored"
          }
          logger.verbose(
            "Skipping listen of \"${it.track_metadata?.track_name ?: "unknown track"}\" $reason"
          )
        }

        isListenSuggestible
      }
      .distinctBy { listen -> listen.listened_at }
      .groupBy {
        // Group all listens that share a Spotify ID
        it.track_metadata!!.additional_info!!.spotify_id
      }
      .map { (_, listens) ->
        // Convert into a Map of Listen to the number of times that Listen was seen.
        listens.first() to listens.size
      }

    trackCounts
      .toLogMessage()
      .also {
        logger.verbose("All listens in time period", it)
      }

    return trackCounts
      .sortedByDescending { (_, count) -> count }
      .take(trackCounts.size / 10)
      .also {
        logger.verbose("Possible choices", it.toLogMessage())
      }
      .randomOrNull(random = random)
      ?.let { (listen, count) ->
        val url = listen.track_metadata?.additional_info?.spotify_id!!
        SuggestedTrack(
          id = url,
          name = listen.track_metadata.track_name!!,
          artist = listen.track_metadata.artist_name!!,
          url = url,
          listenCount = count
        )
      }
  }
}

private fun List<Pair<Listen, Int>>.toLogMessage(): String {
  return joinToString(separator = "\n") { (listen, count) ->
    "$count - ${listen.track_metadata?.track_name!!}"
  }
}