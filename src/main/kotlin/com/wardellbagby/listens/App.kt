package com.wardellbagby.listens

import com.wardellbagby.listens.listenbrainz.ListenBrainzRepository
import com.wardellbagby.listens.targets.Target
import com.wardellbagby.listens.tracks.SongwhipConverter
import com.wardellbagby.listens.tracks.SuggestedTrack
import com.wardellbagby.listens.tracks.SuggestedTrackFormatter
import com.wardellbagby.listens.tracks.TrackSuggester
import io.ktor.utils.io.printStack
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.days

@Single
class App(
  private val listenBrainzRepository: ListenBrainzRepository,
  private val trackSuggester: TrackSuggester,
  private val songwhipConverter: SongwhipConverter,
  private val trackFormatter: SuggestedTrackFormatter,
  private val targets: List<Target>,
  private val configuration: Configuration,
  private val logger: Logger
) {
  suspend fun run() {
    if (configuration.dryRun) {
      logger.warn("Dry run is set; suggested track won't be posted.")
    } else {
      logger.warn("Dry run is not set to true; suggested track will be posted to all available targets.")
    }
    logger.info("Loaded targets", targets.map { it.loggableName })
    // Get a date range representing from a month ago today until right this moment.
    val now = Clock.System.now()
    val lastMonth = now.minus(configuration.relativeStartInDays.days)

    val ignoredSpotifyUrls = configuration.ignoredTracksPath
      .readTextOrEmpty()
      .split("\n")

    logger.verbose("Ignored spotify URLs: $ignoredSpotifyUrls")

    val listens = runCatching {
      listenBrainzRepository.fetchListens(start = lastMonth, end = now)
    }
      .fold(
        onSuccess = { it },
        onFailure = {
          it.printStack()
          return
        }
      )

    logger.verbose("Found ${listens.size} total listens!")

    val suggestedTrack = trackSuggester.generate(listens = listens)
      ?.let {
        logger.info("Selected track", it)

        it.copy(
          url = songwhipConverter.convert(it.url)
        )
      }
      ?: error("Unable to find suggested track!")

    targets.forEach { target ->
      val message = trackFormatter
        .format(track = suggestedTrack, maxLength = target.maxLength)
        .also {
          logger.info("Message for ${target.loggableName}", it)
        }

      if (!configuration.dryRun) {
        target.post(message)
      }
    }
    if (!configuration.dryRun) {
      updateIgnoredTracks(currentIgnoredTracks = ignoredSpotifyUrls, track = suggestedTrack)
    }
  }

  /**
   * Write the track that was suggested to the file specified by [Configuration.ignoredTracksPath].
   */
  private fun updateIgnoredTracks(currentIgnoredTracks: List<String>, track: SuggestedTrack) {
    logger.info("Adding ${track.id} to ignored using file ${configuration.ignoredTracksPath}")

    val content = (currentIgnoredTracks + track.id).joinToString(separator = "\n")

    configuration.ignoredTracksPath
      .writeText(content)
  }

  private fun Path.readTextOrEmpty(): String {
    return if (exists()) {
      readText()
    } else {
      ""
    }
  }
}