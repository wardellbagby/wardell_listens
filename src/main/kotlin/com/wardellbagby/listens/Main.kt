package com.wardellbagby.listens

import io.ktor.utils.io.printStack
import kotlinx.datetime.Clock
import kotlin.io.path.absolutePathString
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
  val now = Clock.System.now()
  val lastWeek = now.minus(7.days)
  val listensResult = runCatching {
    fetchTracks(
      start = lastWeek,
      end = now
    )
  }

  if (listensResult.isFailure) {
    listensResult.exceptionOrNull()!!.printStack()
  }
  val listens = listensResult.getOrThrow()
  println("Found ${listens.size} total listens!")

  val ignoredSpotifyUrls = loadFromResources(ignoredTracksFile)?.split("\n") ?: listOf()
  val suggestedTrack = getSuggestedTrack(
    ignoredSpotifyUrls = ignoredSpotifyUrls,
    listens = listens
  )

  println(suggestedTrack.asTweet())
  postTweet(suggestedTrack.asTweet())
  updateIgnoredTracks(suggestedTrack)
}

private fun updateIgnoredTracks(track: SuggestedTrack) {
  println("Adding ${track.spotifyUrl} to ignored using file ${environment.ignoredTracksOutput.absolutePathString()}")

  environment.ignoredTracksOutput
    .writeText(track.spotifyUrl)
}