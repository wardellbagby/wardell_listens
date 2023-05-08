package com.wardellbagby.listens

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

fun Path.readTextOrEmpty(): String {
  return if (exists()) {
    readText()
  } else {
    ""
  }
}