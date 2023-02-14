package com.wardellbagby.listens

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalContracts::class)
fun Any?.assertNotNull(message: String? = null): Any {
  contract { returns() implies (this@assertNotNull != null) }

  assertNotNull(actual = this, message = message)
  return this
}

fun String?.assertNotBlankOrNull(message: String? = null): String {
  assertNotNull(message = message)
  assertTrue(isNotBlank(), message = message ?: "Expected value to not be blank")
  return this
}