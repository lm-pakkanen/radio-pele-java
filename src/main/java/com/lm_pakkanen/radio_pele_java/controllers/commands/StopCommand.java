package com.lm_pakkanen.radio_pele_java.controllers.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.controllers.MailMan;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.StopEmbed;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

@Component
public final class StopCommand extends BaseCommand implements ICommandListener {

  private final @NonNull TrackScheduler trackScheduler;

  public StopCommand(@Autowired @NonNull TrackScheduler trackScheduler) {
    super();
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

  /**
   * Stops playback and leaves the voice channel.
   * 
   * @throws NullPointerException
   */
  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
      throws NullPointerException {
    if (!event.getName().equals(this.getCommandName())) {
      return;
    }

    try {
      super.getTextChan(event);

      final AudioManager audioManager = event.getGuild().getAudioManager();

      this.trackScheduler.destroy();

      if (audioManager.isConnected()) {
        tryLeaveVoiceChan(event, audioManager);
      }

      MailMan.replyInteractionEmbed(event, new StopEmbed().getEmbed());
    } catch (InvalidChannelException exception) {
      MailMan.replyInteractionEmbed(event,
          new ExceptionEmbed(exception).getEmbed());
    }
  }

  /**
   * Tries to leave the voice channel.
   * 
   * @param event        that initiated the command.
   * @param audioManager instance.
   */
  private void tryLeaveVoiceChan(SlashCommandInteractionEvent event,
      AudioManager audioManager) {
    final AudioChannelUnion audioChan = event.getMember().getVoiceState()
        .getChannel();

    if (audioChan == null || !audioChan.getType().equals(ChannelType.VOICE)) {
      return;
    }

    audioManager.closeAudioConnection();
  }
}
