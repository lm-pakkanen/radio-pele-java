package com.lm_pakkanen.radio_pele_java.controllers.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.controllers.MailMan;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

@Component
public final class StopCommand extends ListenerAdapter
    implements ICommandListener {

  private TrackScheduler trackScheduler;

  public StopCommand(@Autowired TrackScheduler trackScheduler) {
    this.trackScheduler = trackScheduler;
  }

  @Override
  public String getCommandName() {
    return "stop";
  }

  @Override
  public String getCommandDescription() {
    return "Stop playback and leave.";
  }

  @Override
  public SlashCommandData getCommandData() {
    return Commands.slash(this.getCommandName(), this.getCommandDescription());
  }

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
      throws NullPointerException {
    if (!event.getName().equals(this.getCommandName())) {
      return;
    }

    try {
      MessageChannelUnion messageChan = event.getChannel();

      if (!messageChan.getType().equals(ChannelType.TEXT)) {
        throw new InvalidChannelException();
      }

      AudioManager audioManager = event.getGuild().getAudioManager();

      this.trackScheduler.destroy();

      if (audioManager.isConnected()) {
        tryLeaveVoiceChan(event, audioManager);
      }

      MailMan.replyInteractionMessage(event, "Hasta la Vista!");
    } catch (InvalidChannelException exception) {
      MailMan.replyInteractionMessage(event, exception.getMessage());
    }
  }

  private void tryLeaveVoiceChan(SlashCommandInteractionEvent event,
      AudioManager audioManager) {
    AudioChannelUnion audioChan = event.getMember().getVoiceState()
        .getChannel();

    if (audioChan == null || !audioChan.getType().equals(ChannelType.VOICE)) {
      return;
    }

    audioManager.closeAudioConnection();
  }
}
