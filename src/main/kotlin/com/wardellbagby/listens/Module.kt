package com.wardellbagby.listens

import com.wardellbagby.listens.targets.AvailableTargets
import com.wardellbagby.listens.targets.Micropub
import com.wardellbagby.listens.targets.Twitter
import com.wardellbagby.listens.tracks.IgnoredTracks
import io.ktor.client.engine.cio.CIO
import org.koin.dsl.module
import kotlin.random.Random

val appModule = module {
  single {
    createHttpClient(CIO.create())
  }

  single { getEnv().createConfigurationFromEnv() }
  single<Random> { Random }

  factory {
    get<Configuration>().twitterAuthentication!!
  }
  factory {
    get<Configuration>().micropubAuthentication!!
  }

  single<IgnoredTracks> {
    IgnoredTracks(
      get<Configuration>().ignoredTracksPath
        .readTextOrEmpty()
        .split("\n")
    )
  }

  single {
    val configuration: Configuration = get()
    AvailableTargets(
      targets = buildList {
        if (configuration.twitterAuthentication != null) {
          add(get<Twitter>())
        }
        if (configuration.micropubAuthentication != null) {
          add(get<Micropub>())
        }
      }.ifEmpty { error("Cannot run without any targets to post a message to.") }
    )
  }
}