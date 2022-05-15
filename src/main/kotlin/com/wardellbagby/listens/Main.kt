package com.wardellbagby.listens

import io.ktor.utils.io.printStack
import kotlinx.datetime.Clock
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.days

private const val ignoredTracksFile = "ignored.txt"
private const val URL_TEMPLATE = "{URL}"
private const val TWEET_MAX_LENGTH = 280
private const val TWITTER_URL_LENGTH = 23

private val SuggestedTrack.extendedTweetTemplate: String
  get() = """
  This week's song suggestion is:
  
  $name by $artist

  $URL_TEMPLATE
  
  #MusicMonday
  """
    .trimIndent()

private val SuggestedTrack.standardTweetTemplate: String
  get() = """
  This week's song suggestion is:
  
  $name

  $URL_TEMPLATE
  
  #MusicMonday
  """
    .trimIndent()

private const val shortTweetTemplate: String = """
    This week's song suggestion is:

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
private fun SuggestedTrack.asTweet(): String {
  return sequence {
    yield(extendedTweetTemplate to extendedTweetTemplate.replaceUrlTemplate())
    yield(standardTweetTemplate to standardTweetTemplate.replaceUrlTemplate())
    yield(shortTweetTemplate to shortTweetTemplate.replaceUrlTemplate())
  }
    .first { (_, templateWithMockUrl) ->
      templateWithMockUrl.length <= TWEET_MAX_LENGTH
    }
    .let { (template, _) -> template.replaceUrlTemplate(spotifyUrl) }
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
    return
  }

  val listens = listensResult.getOrThrow()
  println("Found ${listens.size} total listens!")

  val suggestedTrack = getSuggestedTrack(
    ignoredSpotifyUrls = ignoredSpotifyUrls,
    listens = listens
  )

  postTweet(suggestedTrack.asTweet().also { println(it) })
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