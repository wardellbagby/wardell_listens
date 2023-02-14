package com.wardellbagby.listens.targets

interface Target {
  val loggableName: String
  val maxLength: Int
  suspend fun post(message: String)
}