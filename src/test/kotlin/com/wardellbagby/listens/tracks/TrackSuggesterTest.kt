package com.wardellbagby.listens.tracks

import com.wardellbagby.listens.FakeLogger
import com.wardellbagby.listens.listenbrainz.AdditionalInfo
import com.wardellbagby.listens.listenbrainz.Listen
import com.wardellbagby.listens.listenbrainz.TrackMetadata
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertTrue

class TrackSuggesterTest {
  private val trackSuggester = TrackSuggester(
    ignoredTracks = IgnoredTracks(listOf("789", "987")),
    logger = FakeLogger(),
    random = Random(0xbeefcafe)
  )

  @Test
  fun `fails to generate with no listens`() {
    val actual = trackSuggester.generate(listOf())

    assertTrue(actual.isEmpty())
  }

  @Test
  fun `fails to generate with less than ten listens`() {
    (1 until 10).forEach {
      val actual = trackSuggester.generate(LISTENS.take(it))

      assertTrue(actual.isEmpty())
    }
  }

  @Test
  fun `filters out listens that do not have a spotify ID`() {
    val listens = defaultListensExcept(
      listensThatMatch = { index, _ -> index > 90 },
      areChangedToBe = { _, listen ->
        listen.copy(
          track_metadata = listen.track_metadata?.copy(
            additional_info = listen.track_metadata?.additional_info?.copy(
              spotify_id = null
            )
          )
        )
      }
    )

    runSuggesterAssertions {
      trackSuggester.generate(listens)
        .also {
          assertTrue(it.isNotEmpty(), message = "Received an empty list of tracks!")
        }
        .forEach { actual ->
          val id = actual.id.toInt()
          assertTrue(id <= 90, message = "Expect $id to be greater than 90")
          assertTrue(actual.url.isNotBlank(), message = "Expect ${actual.url} to not be blank")
        }
    }
  }

  @Test
  fun `filters out listens that are not in the top 10 percent of listens`() {
    val listens = defaultListensExcept(
      listensThatMatch = { index, _ -> index < 20 },
      areChangedToBe = { index, listen ->
        listen.copy(
          track_metadata = listen.track_metadata?.copy(
            additional_info = listen.track_metadata?.additional_info?.copy(
              spotify_id = (index % 10).toString()
            )
          )
        )
      }
    )

    runSuggesterAssertions {
      trackSuggester.generate(listens)
        .also {
          assertTrue(it.isNotEmpty(), message = "Received an empty list of tracks!")
        }
        .forEach { actual ->
          val id = actual.id.toInt()
          assertTrue(id < 20, message = "Expect $id to be greater than 90")
          assertTrue(actual.url.isNotBlank(), message = "Expect ${actual.url} to not be blank")
        }
    }
  }

  private companion object {
    private fun runSuggesterAssertions(assertion: () -> Unit) {
      (0 until 10_000).forEach { _ -> assertion() }
    }

    private fun defaultListensExcept(
      listensThatMatch: (index: Int, Listen) -> Boolean = { _, _ -> true },
      areChangedToBe: (index: Int, Listen) -> Listen
    ): List<Listen> {
      return LISTENS.mapIndexed { index, listen ->
        if (listensThatMatch(index, listen)) {
          areChangedToBe(index, listen)
        } else {
          listen
        }
      }
    }

    private val LISTENS = (0L until 100L).map {
      Listen(
        listened_at = it,
        track_metadata = TrackMetadata(
          artist_name = it.toString(),
          release_name = it.toString(),
          track_name = it.toString(),
          additional_info = AdditionalInfo(
            spotify_id = it.toString()
          )
        )
      )
    }
  }
}