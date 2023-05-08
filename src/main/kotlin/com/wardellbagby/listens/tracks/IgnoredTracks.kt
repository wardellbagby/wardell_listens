package com.wardellbagby.listens.tracks

@JvmInline
value class IgnoredTracks(private val tracks: List<String>) : List<String> by tracks