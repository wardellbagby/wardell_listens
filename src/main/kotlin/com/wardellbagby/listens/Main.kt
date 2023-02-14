package com.wardellbagby.listens

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.logger.Level.INFO
import org.koin.ksp.generated.defaultModule

fun createAppComponent(): Koin {
  return startKoin {
    printLogger(level = INFO)
    modules(defaultModule, appModule)
  }.koin
}

suspend fun main() {
  val component = createAppComponent()
  val app = component.get<App>()
  app.run()
}


