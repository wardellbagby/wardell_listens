package com.wardellbagby.listens

import io.ktor.utils.io.printStack
import kotlinx.datetime.Clock
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.days

private const val ignoredTracksFile = "ignored.txt"
private fun SuggestedTrack.asTweet(): String {
  return """
    This week's song suggestion is:
    
    $name by $artist

    $spotifyUrl"""
    .trimIndent()
}

suspend fun main() {
  // Get a date range representing from a week ago today until right this moment.
  val now = Clock.System.now()
  val lastWeek = now.minus(7.days)

  val ignoredSpotifyUrls = loadFromResources(ignoredTracksFile)
    ?.split("\n")
    ?: emptyList()

  println("Ignored spotify URLs: $ignoredSpotifyUrls")

  val listensResult = runCatching {
    fetchListens(
      start = lastWeek,
      end = now
    )
  }

  if (listensResult.isFailure) {
    listensResult.exceptionOrNull()!!.printStack()
  }
  val listens = listensResult.getOrThrow()
  println("Found ${listens.size} total listens!")

  val suggestedTrack = getSuggestedTrack(
    ignoredSpotifyUrls = ignoredSpotifyUrls,
    listens = listens
  )

  println(suggestedTrack.asTweet())
  postTweet(suggestedTrack.asTweet())
  updateIgnoredTracks(suggestedTrack)
}

/**
 * Write the track that was suggested to the file specified by [Environment.ignoredTracksOutput].
 *
 * We do this so that the GitHub Workflow that will run this can read that file to see what track
 * was tweeted, so that it may then add a new commit to this repo updating ignored.txt with that
 * track.
 */
private fun updateIgnoredTracks(track: SuggestedTrack) {
  println("Adding ${track.spotifyUrl} to ignored using file ${environment.ignoredTracksOutput}")

  environment.ignoredTracksOutput
    .writeText(track.spotifyUrl)
}