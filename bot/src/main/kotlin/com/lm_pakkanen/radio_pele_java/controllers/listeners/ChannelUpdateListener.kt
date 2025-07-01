package com.lm_pakkanen.radio_pele_java.controllers.listeners;

import java.util.List;
import org.springframework.stereotype.Component;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.IEventListener;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.managers.AudioManager;

@Component
@RequiredArgsConstructor
public class ChannelUpdateListener implements IEventListener {

  private final TrackScheduler trackScheduler;

  /**
   * Listens to GuildVoiceUpdateEvents and leaves the voice channel if only bots
   * are left in the bot's connected voice channel.
   */
  @Override
  public void onEvent(GenericEvent event) {

    if (!(event instanceof GuildVoiceUpdateEvent)) {
      // Don't care
      return;
    }

    final GuildVoiceUpdateEvent guildVoiceUpdateEvent = (GuildVoiceUpdateEvent) event;
    final AudioChannelUnion chanUserLeftFrom = guildVoiceUpdateEvent
        .getChannelLeft();

    final AudioManager audioManager = guildVoiceUpdateEvent.getGuild()
        .getAudioManager();

    final AudioChannelUnion connectedAudioChan = audioManager
        .getConnectedChannel();

    if (chanUserLeftFrom == null || connectedAudioChan == null) {
      // Don't care
      return;
    }

    if (!chanUserLeftFrom.getId().equals(connectedAudioChan.getId())) {
      // Don't care
      return;
    }

    final List<Member> membersInChan = connectedAudioChan.getMembers();

    final boolean isAllBotsInChan = membersInChan.stream()
        .allMatch(member -> member.getUser().isBot());

    if (isAllBotsInChan) {
      // Clear queue and leave channel if only bots left in the channel
      trackScheduler.destroy();
      audioManager.closeAudioConnection();
      audioManager.setSendingHandler(null);
    }
  }
}
