package com.wardellbagby.listens.targets

interface Target {
  val loggableName: String
  val maxLength: Int
  suspend fun post(message: String)
}

@JvmInline
value class AvailableTargets(val targets: List<Target>) : List<Target> by targets