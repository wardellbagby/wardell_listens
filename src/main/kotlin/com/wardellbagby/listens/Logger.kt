package com.wardellbagby.listens

import com.wardellbagby.listens.Logger.Level
import org.koin.core.annotation.Single
import java.time.Instant
import java.time.format.DateTimeFormatter

abstract class Logger {
  enum class Level {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
  }

  fun verbose(message: String, vararg args: Any) {
    log(level = Level.VERBOSE, message = message, args = args)
  }

  fun debug(message: String, vararg args: Any) {
    log(level = Level.DEBUG, message = message, args = args)
  }

  fun info(message: String, vararg args: Any) {
    log(level = Level.INFO, message = message, args = args)
  }

  fun warn(message: String, vararg args: Any) {
    log(level = Level.WARN, message = message, args = args)
  }

  fun error(message: String, vararg args: Any) {
    log(level = Level.ERROR, message = message, args = args)
  }

  abstract fun log(level: Level, message: String, vararg args: Any)

  protected fun Array<out Any>.format(): String {
    val formatted = map {
      when (it) {
        is Throwable -> it.stackTraceToString()
        else -> it.toString()
      }
    }
      .filter { it.isNotBlank() }
      .joinToString(separator = "\n")
      .lines()
      .joinToString(separator = "\n") { "\t$it" }
      .trim()

    return if (formatted.isBlank()) {
      ""
    } else {
      "\n$formatted"
    }
  }
}

@Single
class RealLogger : Logger() {
  private val dateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  override fun log(level: Level, message: String, vararg args: Any) {
    val time = dateTimeFormatter.format(Instant.now())
    val formattedArgs = args.format()

    println("$time [${level.name}] $message$formattedArgs")
  }
}