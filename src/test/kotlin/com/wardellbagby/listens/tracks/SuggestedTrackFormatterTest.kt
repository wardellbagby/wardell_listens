package com.wardellbagby.listens.tracks

import org.junit.Test
import kotlin.test.assertTrue

class SuggestedTrackFormatterTest {
  private val formatter = SuggestedTrackFormatter()

  @Test
  fun `respects max length`() {
    val track = SuggestedTrack(
      id = "1",
      name = "A".repeat(100),
      artist = "B".repeat(100),
      url = "C".repeat(100),
      listenCount = 1
    )
    (MINIMUM_EXPECTED_POST_LENGTH until MAXIMUM_EXPECTED_POST_LENGTH).forEach { maxLength ->
      val actual = formatter.format(track = track, maxLength = maxLength)
      assertTrue(actual.length <= maxLength)
    }
  }

  private companion object {
    private const val MINIMUM_EXPECTED_POST_LENGTH = 100
    private const val MAXIMUM_EXPECTED_POST_LENGTH = 500
  }
}