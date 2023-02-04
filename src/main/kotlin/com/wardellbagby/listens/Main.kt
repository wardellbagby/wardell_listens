package com.wardellbagby.listens

import com.wardellbagby.listens.Target.Micropub
import com.wardellbagby.listens.Target.Twitter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.printStack
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import post
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.days

private const val SONGWHIP_ENDPOINT = "https://songwhip.com/"
private const val URL_TEMPLATE = "{URL}"
private const val POST_MAX_LENGTH = 280
private const val TWITTER_URL_LENGTH = 23

private const val POST_HEADER = "This week's song is:"
private val SuggestedTrack.extendedPostTemplate: String
  get() = """
  $POST_HEADER
  
  $name by $artist

  $URL_TEMPLATE
  
  #MusicMonday
  """
    .trimIndent()

private val SuggestedTrack.standardPostTemplate: String
  get() = """
  $POST_HEADER
  
  $name

  $URL_TEMPLATE
  
  #MusicMonday
  """
    .trimIndent()

private const val shortPostTemplate: String = """
    $POST_HEADER

    $URL_TEMPLATE
    
    #MusicMonday
    """

/**
 * Replaces instances of [URL_TEMPLATE] with [replacement].
 *
 * By default, [replacement] is a string of length [TWITTER_URL_LENGTH], which enables accurately
 * knowing the length of a post before we post it.
 */
private fun String.replaceUrlTemplate(
  replacement: String = "A".repeat(TWITTER_URL_LENGTH)
): String = replace(
  URL_TEMPLATE,
  replacement
)

/**
 * Convert this [SuggestedTrack] into a postable message. The format of the post depends on how
 * long the title and artist of this [SuggestedTrack] is. This will choose the longest possible
 * message that doesn't go over [POST_MAX_LENGTH].
 */
private suspend fun SuggestedTrack.toPostableMessage(): String {
  return sequence {
    yield(extendedPostTemplate to extendedPostTemplate.replaceUrlTemplate())
    yield(standardPostTemplate to standardPostTemplate.replaceUrlTemplate())
    yield(shortPostTemplate to shortPostTemplate.replaceUrlTemplate())
  }
    .first { (_, templateWithMockUrl) ->
      templateWithMockUrl.length <= POST_MAX_LENGTH
    }
    .let { (template, _) -> template.replaceUrlTemplate(spotifyUrl.toSongwhipUrl()) }
}

suspend fun main() {
  // Get a date range representing from a month ago today until right this moment.
  val now = Clock.System.now()
  val lastMonth = now.minus(30.days)

  val ignoredSpotifyUrls = environment.ignoredTracksPath
    .readTextOrEmpty()
    .split("\n")

  println("Ignored spotify URLs: $ignoredSpotifyUrls")

  val listensResult = runCatching {
    fetchListens(
      start = lastMonth,
      end = now
    )
  }

  if (listensResult.isFailure) {
    listensResult.exceptionOrNull()!!.printStack()
    return
  }

  val listens = listensResult.getOrThrow()
  println("Found ${listens.size} total listens!")

  val suggestedTrack = getSuggestedTrack(
    ignoredSpotifyUrls = ignoredSpotifyUrls,
    listens = listens
  )
    ?.also {
      println("Selected track")
      println(it)
    }
    ?: error("Unable to find suggested track!")

  val message = suggestedTrack.toPostableMessage().also {
    println("Post:")
    println(it)
  }

  environment.targets.forEach {
    it.post(message)
  }
  updateIgnoredTracks(currentIgnoredTracks = ignoredSpotifyUrls, track = suggestedTrack)
}

/**
 * Write the track that was suggested to the file specified by [Environment.ignoredTracksPath].
 */
private fun updateIgnoredTracks(currentIgnoredTracks: List<String>, track: SuggestedTrack) {
  println("Adding ${track.spotifyUrl} to ignored using file ${environment.ignoredTracksPath}")

  val content = (currentIgnoredTracks + track.spotifyUrl).joinToString(separator = "\n")

  environment.ignoredTracksPath
    .writeText(content)
}

@Serializable
private data class SongwhipResponse(
  val url: String
)

private suspend fun String.toSongwhipUrl(): String {
  println("Converting URL \"$this\" to a Songwhip URL")
  return runCatching {
    httpClient.post<SongwhipResponse>(urlString = SONGWHIP_ENDPOINT) {
      contentType(ContentType.Application.Json)
      body = mapOf("url" to this@toSongwhipUrl)
    }
      .url
      .also {
        println("Received Songwhip URL: $it")
      }
  }
    .onFailure {
      println("Failed to convert; falling back to Spotify URL")
      it.printStackTrace()
    }
    .getOrDefault(this)
}

private suspend fun Target.post(message: String) {
  when (this) {
    is Micropub -> post(message)
    // This is gonna stop working pretty soon anyway so just ignore the errors it's going to start
    // throwing.
    is Twitter -> runCatching { post(message) }
  }
}

private fun Path.readTextOrEmpty(): String {
  return if (exists()) {
    readText()
  } else {
    ""
  }
}