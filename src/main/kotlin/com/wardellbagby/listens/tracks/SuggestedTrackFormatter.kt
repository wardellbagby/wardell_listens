package com.wardellbagby.listens.tracks

import org.koin.core.annotation.Single

@Single
class SuggestedTrackFormatter {

  /**
   * Convert [track] into a postable message. The format of the post depends on how
   * long the title and artist of this [track] is. This will choose the longest possible
   * message that doesn't go over [maxLength].
   */
  fun format(track: SuggestedTrack, maxLength: Int): String = with(track) {
    return sequence {
      yield(extendedPostTemplate.replaceUrlTemplate(replacement = url))
      yield(standardPostTemplate.replaceUrlTemplate(replacement = url))
      yield(shortPostTemplate.replaceUrlTemplate(replacement = url))
      yield(minimumPostTemplate.replaceUrlTemplate(replacement = url))
    }
      .firstOrNull { it.length <= maxLength }
      ?: error("No templates supported for length $maxLength.")
  }

  private fun String.replaceUrlTemplate(
    replacement: String
  ): String = replace(
    URL_TEMPLATE,
    replacement
  )

  private companion object {
    private val URL_TEMPLATE = "{${"A".repeat(23)}}"
    private const val POST_MAX_LENGTH = 280

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

    private val shortPostTemplate: String = """
    $POST_HEADER

    $URL_TEMPLATE
    
    #MusicMonday
    """
      .trimIndent()

    private val minimumPostTemplate: String = URL_TEMPLATE
  }
}