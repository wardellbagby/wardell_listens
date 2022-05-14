package com.wardellbagby.listens

fun loadFromResources(filename: String): String? {
  return object {}::class.java.getResourceAsStream(filename)
    ?.bufferedReader()
    ?.use {
      it.readText()
    }
}