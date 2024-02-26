package com.lm_pakkanen.radio_pele_java.controllers.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.controllers.MailMan;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.models.Store;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;
import com.lm_pakkanen.radio_pele_java.models.exceptions.NotInChannelException;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.SongAddedEmbed;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

@Component
public final class PlayCommand extends BaseCommand implements ICommandListener {

  private final @NonNull Store store;
  private final @NonNull TrackScheduler trackScheduler;

  public PlayCommand(@Autowired @NonNull Store store,
      @Autowired @NonNull TrackScheduler trackScheduler) {
    super();
    this.store = store;
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
          url, true);

      final AudioManager audioManager = event.getGuild().getAudioManager();
      final boolean isConnected = audioManager.isConnected();
      final AudioChannel memberAudioChan = tryGetMemberVoiceChan(event);

      final AudioChannelUnion managerAudioChan = audioManager
          .getConnectedChannel();

      long managerAudioChanId = 0L;

      if (managerAudioChan != null) {
        managerAudioChanId = managerAudioChan.getIdLong();
      }

      final long memberAudioChanId = memberAudioChan.getIdLong();

      if (!isConnected || managerAudioChanId != memberAudioChanId) {
        connectToVoiceChan(memberAudioChan, audioManager);
      }

      if (audioManager.getSendingHandler() == null) {
        audioManager
            .setSendingHandler(this.trackScheduler.getRapAudioSendHandler());
        audioManager.setSelfDeafened(true);
      }

      if (!this.trackScheduler.isPlaying()) {
        this.trackScheduler.play();
      }

      MailMan.replyInteraction(event,
          new SongAddedEmbed(addedTrack, this.store).getEmbed());
    } catch (InvalidChannelException | NotInChannelException
        | FailedToLoadSongException exception) {
      MailMan.replyInteraction(event, new ExceptionEmbed(exception).getEmbed());
    }
  }

  /**
   * Tries to get the voice channel of the member who initiated the command.
   * 
   * @param event that initiated the command.
   * @throws NotInChannelException
   */
  private @NonNull AudioChannel tryGetMemberVoiceChan(
      @NonNull SlashCommandInteractionEvent event)
      throws NotInChannelException {
    final AudioChannelUnion audioChan = event.getMember().getVoiceState()
        .getChannel();

    if (audioChan == null || !audioChan.getType().equals(ChannelType.VOICE)) {
      throw new NotInChannelException();
    }

    return (AudioChannel) audioChan;
  }

  /**
   * Connects bot to the voice channel.
   * 
   * @param audioManager instance.
   */
  private void connectToVoiceChan(@NonNull AudioChannel audioChan,
      AudioManager audioManager) {
    audioManager.openAudioConnection(audioChan);
  }
}
