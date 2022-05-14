package com.wardellbagby.listens

private object MyClass

fun loadFromResources(filename: String): String? {
  return MyClass::class.java.classLoader.getResourceAsStream(filename)
    ?.bufferedReader()
    ?.use {
      it.readText()
    }
}