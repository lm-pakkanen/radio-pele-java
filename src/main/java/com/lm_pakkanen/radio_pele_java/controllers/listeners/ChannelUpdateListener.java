package com.lm_pakkanen.radio_pele_java.controllers.listeners;

import java.util.List;

import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.interfaces.IEventListener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

@Component
public class ChannelUpdateListener implements IEventListener {

  @Override
  public void onEvent(GenericEvent event) {
    if (!(event instanceof GuildVoiceUpdateEvent)) {
      // Don't care
      return;
    }

    GuildVoiceUpdateEvent guildVoiceUpdateEvent = (GuildVoiceUpdateEvent) event;
    AudioChannelUnion chanUserLeftFrom = guildVoiceUpdateEvent.getChannelLeft();

    AudioChannelUnion connectedAudioChan = guildVoiceUpdateEvent.getGuild()
        .getAudioManager().getConnectedChannel();

    if (chanUserLeftFrom == null || connectedAudioChan == null) {
      // Don't care
      return;
    }

    if (!chanUserLeftFrom.getId().equals(connectedAudioChan.getId())) {
      // Don't care
      return;
    }

    List<Member> membersInChan = connectedAudioChan.getMembers();

    boolean isAllBotsInChan = membersInChan.stream()
        .allMatch(member -> member.getUser().isBot());

    if (isAllBotsInChan) {
      // Leae if only bots left in channel
      connectedAudioChan.getGuild().getAudioManager().closeAudioConnection();
    }
  }
}
