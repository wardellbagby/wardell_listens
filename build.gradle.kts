plugins {
  kotlin("jvm") version "1.6.21"
  kotlin("plugin.serialization") version "1.6.21"
  id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "com.wardellbagby"

repositories {
  mavenCentral()
}

tasks.jar {
  archiveFileName.set("dist.jar")
  manifest {
    attributes["Main-Class"] = "com.wardellbagby.listens.MainKt"
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
  implementation("io.ktor:ktor-client-core:1.6.7")
  implementation("io.ktor:ktor-client-cio:1.6.7")
  implementation("io.ktor:ktor-client-serialization:1.6.7")
  implementation("blue.starry:penicillin:6.2.3")
}

tasks {

  shadowJar {
    archiveClassifier.set("")
  }
}