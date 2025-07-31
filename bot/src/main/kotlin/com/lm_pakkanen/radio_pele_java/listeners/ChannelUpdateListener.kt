package com.lm_pakkanen.radio_pele_java.listeners

import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler
import com.lm_pakkanen.radio_pele_java.interfaces.IEventListener
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.GuildVoiceState
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

    if (chanUserLeftFrom == null) {
      // Don't care
      log.debug { "No user channel available" }
      return
    }

    val botVoiceState: GuildVoiceState? = event.guild.selfMember.voiceState

    if (botVoiceState == null) {
      // Don't care
      log.debug { "No bot channel available" }
      return
    }

    val botChan: AudioChannel = botVoiceState.channel!!

    log.debug { "User left from channel '$chanUserLeftFrom.name'" }
    log.debug { "Bot's current channel: '$botChan.name'" }

    if (chanUserLeftFrom.id != botChan.id) {
      // Don't care
      log.debug { "User left from channel other than bot channel" }
      return
    }

    val membersInChan = botChan.members
    val isAllBotsInChan = membersInChan.stream().allMatch { member -> member.user.isBot }

    log.debug { "Only bots left in the channel: $isAllBotsInChan" }

    if (isAllBotsInChan) {
      // Clear queue and leave channel if only bots left in the channel
      trackScheduler.destroy()
      event.jda.directAudioController.disconnect(event.guild)
    }
  }
}
