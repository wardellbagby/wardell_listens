import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("jvm") version "1.8.10"
  kotlin("plugin.serialization") version "1.8.10"
  id("com.google.devtools.ksp") version "1.8.10-1.0.9"
  application
}

group = "com.wardellbagby"

repositories {
  mavenCentral()
}

application {
  mainClass.set("com.wardellbagby.listens.MainKt")
}

kotlin {
  jvmToolchain(11)
}

sourceSets.main {
  java.srcDirs("build/generated/ksp/main/kotlin")
}

tasks.test {
  testLogging {
    events = setOf(
      TestLogEvent.FAILED,
      TestLogEvent.PASSED,
      TestLogEvent.SKIPPED,
      TestLogEvent.STANDARD_OUT
    )
    exceptionFormat = TestExceptionFormat.FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true
  }
}

dependencies {
  ksp("io.insert-koin:koin-ksp-compiler:1.1.0")
  implementation("blue.starry:penicillin:6.3.0")
  implementation("io.insert-koin:koin-annotations:1.1.0")
  implementation("io.insert-koin:koin-core:3.3.2")
  implementation("io.ktor:ktor-client-cio:2.2.3")
  implementation("io.ktor:ktor-client-content-negotiation:2.2.3")
  implementation("io.ktor:ktor-client-core:2.2.3")
  implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.3")
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")

  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("io.ktor:ktor-client-mock:2.2.3")
}