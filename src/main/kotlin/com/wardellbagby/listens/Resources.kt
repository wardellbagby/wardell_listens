package com.wardellbagby.listens

/**
 * A class that is only used to fetch a class loader so that resources that can loaded. Should never
 * be used for anything else.
 */
private object Resources

/**
 * Load the given file from the resources folder and return its content as a String.
 */
fun loadFromResources(filename: String): String? {
  return Resources::class.java.classLoader.getResourceAsStream(filename)
    ?.bufferedReader()
    ?.use {
      it.readText()
    }
}