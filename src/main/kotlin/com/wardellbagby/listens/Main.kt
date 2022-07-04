package com.wardellbagby.listens

import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.printStack
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.days

private const val SONGWHIP_ENDPOINT = "https://songwhip.com/"
private const val URL_TEMPLATE = "{URL}"
private const val TWEET_MAX_LENGTH = 280
private const val TWITTER_URL_LENGTH = 23

private const val TWEET_HEADER = "This week's song is:"
private val SuggestedTrack.extendedTweetTemplate: String
  get() = """
  $TWEET_HEADER
  
  $name by $artist

  $URL_TEMPLATE
  
  #MusicMonday
  """
    .trimIndent()

private val SuggestedTrack.standardTweetTemplate: String
  get() = """
  $TWEET_HEADER
  
  $name

  $URL_TEMPLATE
  
  #MusicMonday
  """
    .trimIndent()

private const val shortTweetTemplate: String = """
    $TWEET_HEADER

    $URL_TEMPLATE
    
    #MusicMonday
    """

/**
 * Replaces instances of [URL_TEMPLATE] with [replacement].
 *
 * By default, [replacement] is a string of length [TWITTER_URL_LENGTH], which enables accurately
 * knowing the length of a tweet before we post it.
 */
private fun String.replaceUrlTemplate(
  replacement: String = "A".repeat(TWITTER_URL_LENGTH)
): String = replace(
  URL_TEMPLATE,
  replacement
)

/**
 * Convert this [SuggestedTrack] into a tweet. The format of the Tweet depends on how long the title
 * and artist of this [SuggestedTrack] is. This will choose the longest possible tweet that doesn't
 * go over [TWEET_MAX_LENGTH].
 */
private suspend fun SuggestedTrack.toTweet(): String {
  return sequence {
    yield(extendedTweetTemplate to extendedTweetTemplate.replaceUrlTemplate())
    yield(standardTweetTemplate to standardTweetTemplate.replaceUrlTemplate())
    yield(shortTweetTemplate to shortTweetTemplate.replaceUrlTemplate())
  }
    .first { (_, templateWithMockUrl) ->
      templateWithMockUrl.length <= TWEET_MAX_LENGTH
    }
    .let { (template, _) -> template.replaceUrlTemplate(spotifyUrl.toSongwhipUrl()) }
}

suspend fun main() {
  // Get a date range representing from a month ago today until right this moment.
  val now = Clock.System.now()
  val lastWeek = now.minus(30.days)

  val ignoredSpotifyUrls = environment.ignoredTracksPath
    .readText()
    .split("\n")

  println("Ignored spotify URLs: $ignoredSpotifyUrls")

  val listensResult = runCatching {
    fetchListens(
      start = lastWeek,
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

  val tweet = suggestedTrack.toTweet().also {
    println("Tweet:")
    println(it)
  }

  postTweet(tweet)
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