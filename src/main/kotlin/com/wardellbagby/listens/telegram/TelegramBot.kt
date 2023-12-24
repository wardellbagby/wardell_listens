package com.wardellbagby.listens.telegram

import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.ParseMode
import com.wardellbagby.listens.tracks.SuggestedTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
class TelegramBot(
  private val authentication: TelegramAuthentication? = null
) {

  suspend fun getSuggestedTrack(
    suggestions: List<SuggestedTrack>
  ): SuggestedTrack? = withContext(Dispatchers.IO) {
    // Yes, it's gross to just do this comparison to current time millis, but it's good enough to stop us from
    // processing messages that happen before we actually send the suggestions. Otherwise, if a user enters multiple
    // selections in the chat before we post our suggestion, we'll immediately select it if it's a valid index.
    // And Telegram date is in seconds, not millis, hence the divide by 1000.
    val startTime = System.currentTimeMillis() / 1000
    if (authentication == null) {
      error("Cannot run Telegram bot without a valid Telegram authentication!")
    }

    val channel = Channel<SuggestedTrack?>()

    val chatId = authentication.chatId.toChatId()
    val bot = Bot.createPolling(authentication.botToken)

    bot.sendInitialMessage(chatId, suggestions)

    bot.start()
    bot.onMessage { message ->
      val messageText = message.text
      if (message.chat.id != authentication.chatId || message.date <= startTime) {
        return@onMessage
      }

      val selectedTrackIndex = messageText?.toIntOrNull()?.minus(1)
      if (selectedTrackIndex == null ||
        selectedTrackIndex < 0 ||
        selectedTrackIndex > suggestions.lastIndex
      ) {
        bot.sendInitialMessage(chatId, suggestions)
        return@onMessage
      } else {
        val selectedTrack = suggestions[selectedTrackIndex]
        bot.sendMessage(chatId, text = selectedTrack.formatTelegramMessageForSelected())
        bot.stop()
        channel.send(selectedTrack)
      }
    }

    channel.receive()
  }

  private suspend fun Bot.sendInitialMessage(
    chatId: ChatId,
    tracks: List<SuggestedTrack>
  ) {
    sendMessage(
      chatId = chatId,
      text = tracks.formatTelegramMessage(),
      parseMode = ParseMode.Markdown,
      disableWebPagePreview = true
    )
  }

  private fun List<SuggestedTrack>.formatTelegramMessage(): String {
    val tracks = mapIndexed { index, suggestedTrack ->
      "${index + 1}: [${suggestedTrack.name} by ${suggestedTrack.artist}](${suggestedTrack.url})"
    }
    return "Enter the number of the track to post:\n\n" + tracks.joinToString(separator = "\n")
  }

  private fun SuggestedTrack.formatTelegramMessageForSelected(): String {
    return "Selected track $name by $artist!"
  }
}