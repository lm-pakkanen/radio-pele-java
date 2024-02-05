package com.lm_pakkanen.radio_pele_java.controllers.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.controllers.MailMan;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;
import com.lm_pakkanen.radio_pele_java.models.exceptions.NotInChannelException;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

@Component
public final class PlayCommand extends BaseCommand implements ICommandListener {

  private @NonNull TrackScheduler trackScheduler;

  public PlayCommand(@Autowired @NonNull TrackScheduler trackScheduler) {
    super();
    this.trackScheduler = trackScheduler;
  }

  @Override
  public String getCommandName() {
    return "play";
  }

  @Override
  public String getCommandDescription() {
    return "Play song from a given URL or resume playback if paused.";
  }

  @Override
  public SlashCommandData getCommandData() {
    return Commands.slash(this.getCommandName(), this.getCommandDescription())
        .addOption(OptionType.STRING, "url", "The URL of the song to play",
            true, false);
  }

  /**
   * Connects to the voice channel, adds the requested song to the queue and
   * starts playback if necessary.
   * 
   * @param event that initiated the command.
   */
  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
      throws NullPointerException {
    if (!event.getName().equals(this.getCommandName())) {
      return;
    }

    event.deferReply().queue();

    try {
      TextChannel textChan = super.getTextChan(event);

      final String url = event.getOption("url").getAsString();
      this.trackScheduler.addToQueue(textChan, url);

      final AudioManager audioManager = event.getGuild().getAudioManager();

      if (!audioManager.isConnected()) {
        tryConnectToVoiceChan(event, audioManager);
      }

      event.getGuild().getAudioManager()
          .setSendingHandler(this.trackScheduler.getRapAudioSendHandler());

      if (!this.trackScheduler.isPlaying()) {
        this.trackScheduler.play();
      }

      MailMan.replyInteractionMessage(event, "Song added to Q!");
    } catch (InvalidChannelException | NotInChannelException
        | FailedToLoadSongException exception) {

      String exceptionMessage = exception.getMessage();

      if (exceptionMessage == null) {
        exceptionMessage = "Unknown exception occurred.";
      }

      MailMan.replyInteractionMessage(event, exceptionMessage);
    }
  }

  /**
   * Tries to connect to the voice channel.
   * 
   * @param event        that initiated the command.
   * @param audioManager instance.
   * @throws NotInChannelException
   */
  private void tryConnectToVoiceChan(SlashCommandInteractionEvent event,
      AudioManager audioManager) throws NotInChannelException {
    final AudioChannelUnion audioChan = event.getMember().getVoiceState()
        .getChannel();

    if (audioChan == null || !audioChan.getType().equals(ChannelType.VOICE)) {
      throw new NotInChannelException();
    }

    audioManager.openAudioConnection(audioChan);
  }
}
