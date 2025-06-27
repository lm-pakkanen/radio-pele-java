package com.lm_pakkanen.radio_pele_java.commands;

import org.springframework.context.annotation.Lazy;
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
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

@Lazy
@Component
@RequiredArgsConstructor
public final class PlayListCommand extends BaseCommand
    implements ICommandListener {

  private final Store store;
  private final TrackScheduler trackScheduler;
  private final LavalinkClient lavalinkClient;

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
      final long guildId = event.getGuild().getIdLong();

      final String url = event.getOption("url").getAsString();
      final Track addedTrack = this.trackScheduler.addToQueue(textChan, guildId,
          url, false);

      final AudioManager audioManager = event.getGuild().getAudioManager();
      connectToVoiceChan(event);

      if (!audioManager.isSelfDeafened()) {
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

  private void connectToVoiceChan(SlashCommandInteractionEvent event)
      throws NotInChannelException {

    final AudioChannel memberVoiceChannel = tryGetMemberVoiceChan(event);
    event.getJDA().getDirectAudioController().connect(memberVoiceChannel);
  }

  /**
   * Tries to get the voice channel of the member who initiated the command.
   * 
   * @param event that initiated the command.
   * @throws NotInChannelException
   */
  private AudioChannel tryGetMemberVoiceChan(SlashCommandInteractionEvent event)
      throws NotInChannelException {

    final Member member = event.getMember();
    final GuildVoiceState memberVoiceState = member.getVoiceState();

    final AudioChannelUnion memberChannel = memberVoiceState.getChannel();

    if (!memberVoiceState.inAudioChannel()) {
      throw new NotInChannelException();
    }

    return memberChannel;
  }
}
