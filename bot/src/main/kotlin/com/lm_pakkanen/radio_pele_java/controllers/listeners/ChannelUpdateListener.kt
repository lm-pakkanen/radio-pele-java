package com.lm_pakkanen.radio_pele_java.controllers.listeners

import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler
import com.lm_pakkanen.radio_pele_java.interfaces.IEventListener
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import org.springframework.stereotype.Component

@Component
class ChannelUpdateListener(
  private val trackScheduler: TrackScheduler
) : IEventListener {

  /**
   * Listens to GuildVoiceUpdateEvents and leaves the voice channel if only bots
   * are left in the bot's connected voice channel.
   */
  override fun onEvent(event: GenericEvent) {

    if (event !is GuildVoiceUpdateEvent) {
      // Don't care
      return
    }

    val chanUserLeftFrom: AudioChannel? = event.channelJoined
    val audioManager = event.getGuild().audioManager
    val connectedAudioChan: AudioChannel? = audioManager.connectedChannel

    if (chanUserLeftFrom == null || connectedAudioChan == null) {
      // Don't care
      return
    }

    if (chanUserLeftFrom.id != connectedAudioChan.id) {
      // Don't care
      return
    }

    val membersInChan = connectedAudioChan.members

    val isAllBotsInChan = membersInChan.stream().allMatch { member -> member.user.isBot }

    if (isAllBotsInChan) {
      // Clear queue and leave channel if only bots left in the channel
      trackScheduler.destroy()
      audioManager.closeAudioConnection()
      audioManager.sendingHandler = null
    }
  }
}
