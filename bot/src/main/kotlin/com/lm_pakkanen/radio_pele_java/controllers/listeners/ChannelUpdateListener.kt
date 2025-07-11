package com.lm_pakkanen.radio_pele_java.controllers.listeners

import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler
import com.lm_pakkanen.radio_pele_java.interfaces.IEventListener
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class ChannelUpdateListener(
  private val trackScheduler: TrackScheduler
) : IEventListener {


  /**
   * Listens to GuildVoiceUpdateEvents and leaves the voice channel if only bots
   * are left in the bot's connected voice channel.
   */
  override fun onEvent(event: GenericEvent) {

    log.debug { "Received event: $event" }

    if (event !is GuildVoiceUpdateEvent) {
      // Don't care
      return
    }

    val chanUserLeftFrom: AudioChannel? = event.channelJoined
    val audioManager = event.getGuild().audioManager
    val connectedAudioChan: AudioChannel? = audioManager.connectedChannel

    log.debug { "User left from channel '$chanUserLeftFrom'" }
    log.debug { "Bot's current channel: '$connectedAudioChan'" }

    if (chanUserLeftFrom == null || connectedAudioChan == null) {
      // Don't care
      log.debug { "No user channel or bot channel available" }
      return
    }

    if (chanUserLeftFrom.id != connectedAudioChan.id) {
      // Don't care
      log.debug { "User left from channel other than bot channel" }
      return
    }

    val membersInChan = connectedAudioChan.members
    val isAllBotsInChan = membersInChan.stream().allMatch { member -> member.user.isBot }

    log.debug { "Only bots left in the channel: $isAllBotsInChan" }

    if (isAllBotsInChan) {
      // Clear queue and leave channel if only bots left in the channel
      trackScheduler.destroy()
      audioManager.closeAudioConnection()
      audioManager.sendingHandler = null
    }
  }
}
