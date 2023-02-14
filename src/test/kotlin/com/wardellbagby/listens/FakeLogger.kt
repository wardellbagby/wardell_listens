package com.wardellbagby.listens

import com.wardellbagby.listens.Logger.Level.ERROR
import com.wardellbagby.listens.Logger.Level.WARN

class FakeLogger : Logger() {
  override fun log(level: Level, message: String, vararg args: Any) {
    when (level) {
      WARN -> println("[WARN] $message${args.format()}")
      ERROR -> System.err.println("[ERROR] $message${args.format()}")
      else -> {}
    }
  }
}