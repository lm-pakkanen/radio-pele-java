package com.lm_pakkanen.radio_pele_java.listeners

import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler
import com.lm_pakkanen.radio_pele_java.interfaces.IEventListener
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class ChannelUpdateListener(
    private val trackScheduler: TrackScheduler,
) : IEventListener {
    /**
     * Listens to GuildVoiceUpdateEvents and leaves the voice channel if only bots
     * are left in the bot's connected voice channel.
     */
    override fun onEvent(event: GenericEvent) {
        log.trace { "Received event: $event" }

        if (event !is GuildVoiceUpdateEvent) {
            // Don't care
            return
        }

        val chanUserLeftFrom: AudioChannel? = event.channelLeft

        if (chanUserLeftFrom == null) {
            // Don't care
            log.trace { "No user channel available" }
            return
        }

        val botVoiceChannel: AudioChannelUnion? =
            event.guild.selfMember.voiceState
                ?.channel

        if (botVoiceChannel == null || botVoiceChannel !is VoiceChannel) {
            // Don't care
            log.trace { "No bot channel available, or the channel is not of the correct type." }
            return
        }

        log.debug { "User left from channel '$chanUserLeftFrom.name'" }
        log.debug { "Bot's current channel: '$botVoiceChannel.name'" }

        if (chanUserLeftFrom.id != botVoiceChannel.id) {
            // Don't care
            log.debug { "User left from channel other than bot channel" }
            return
        }

        val membersInChan = botVoiceChannel.members
        val isAllBotsInChan = membersInChan.stream().allMatch { member -> member.user.isBot }

        log.debug { "Only bots left in the channel: $isAllBotsInChan" }

        if (isAllBotsInChan) {
            // Clear queue and leave channel if only bots left in the channel
      trackScheduler.destroy()
      event.jda.directAudioController.disconnect(event.guild)
    }
  }
}
