package com.wardellbagby.listens.listenbrainz

import kotlinx.serialization.Serializable

@Serializable
data class ListensResponse(val payload: Payload)

@Serializable
data class Payload(val listens: List<Listen>)

@Serializable
data class AdditionalInfo(val spotify_id: String? = null)

@Serializable
data class Listen(val listened_at: Long, val track_metadata: TrackMetadata? = null)

@Serializable
data class TrackMetadata(
  val additional_info: AdditionalInfo? = null,
  val artist_name: String? = null,
  val release_name: String? = null,
  val track_name: String? = null,
)