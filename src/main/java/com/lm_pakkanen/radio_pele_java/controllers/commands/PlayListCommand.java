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
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.SongAddedEmbed;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

@Component
public final class PlayListCommand extends BaseCommand
    implements ICommandListener {

  private final @NonNull TrackScheduler trackScheduler;

  public PlayListCommand(@Autowired @NonNull TrackScheduler trackScheduler) {
    super();
    this.trackScheduler = trackScheduler;
  }

  @Override
  public String getCommandName() {
    return "playlist";
  }

  @Override
  public String getCommandDescription() {
    return "Play playlist from a given URL.";
  }

  @Override
  public SlashCommandData getCommandData() {
    return Commands.slash(this.getCommandName(), this.getCommandDescription())
        .addOption(OptionType.STRING, "url", "The URL of the playlist to play",
            true, false);
  }

  /**
   * Connects to the voice channel, adds the requested song to the queue and
   * starts playback if necessary.
   * 
   * @param event that initiated the command.
   * @throws NullPointerException
   */
  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
      throws NullPointerException {
    if (!event.getName().equals(this.getCommandName())) {
      return;
    }

    event.deferReply().queue();

    try {
      final TextChannel textChan = super.getTextChan(event);

      final String url = event.getOption("url").getAsString();
      final AudioTrack addedTrack = this.trackScheduler.addToQueue(textChan,
          url);

      final AudioManager audioManager = event.getGuild().getAudioManager();
      audioManager.setSelfDeafened(true);

      if (!audioManager.isConnected()) {
        tryConnectToVoiceChan(event, audioManager);
      }

      event.getGuild().getAudioManager()
          .setSendingHandler(this.trackScheduler.getRapAudioSendHandler());

      if (!this.trackScheduler.isPlaying()) {
        this.trackScheduler.play();
      }

      MailMan.replyInteractionEmbed(event,
          new SongAddedEmbed(addedTrack, this.trackScheduler.getStore())
              .getEmbed());
    } catch (InvalidChannelException | NotInChannelException
        | FailedToLoadSongException exception) {
      MailMan.replyInteractionEmbed(event,
          new ExceptionEmbed(exception).getEmbed());
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