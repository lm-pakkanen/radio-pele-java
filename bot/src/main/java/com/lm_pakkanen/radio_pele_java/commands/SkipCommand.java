package com.lm_pakkanen.radio_pele_java.commands;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import com.lm_pakkanen.radio_pele_java.controllers.MailMan;
import com.lm_pakkanen.radio_pele_java.controllers.TrackScheduler;
import com.lm_pakkanen.radio_pele_java.interfaces.ICommandListener;
import com.lm_pakkanen.radio_pele_java.models.Store;
import com.lm_pakkanen.radio_pele_java.models.exceptions.InvalidChannelException;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.CurrentSongEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.ExceptionEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.QueueEmptyEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.SongSkippedEmbed;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
@Lazy
public final class SkipCommand extends BaseCommand implements ICommandListener {

  private final @NonNull Store store;
  private final @NonNull TrackScheduler trackScheduler;

  public SkipCommand(@Autowired @NonNull Store store,
      @Autowired @NonNull TrackScheduler trackScheduler) {
    super();
    this.store = store;
    this.trackScheduler = trackScheduler;
  }

  @Override
  public String getCommandName() {
    return "skip";
  }

  @Override
  public String getCommandDescription() {
    return "Skip the current song.";
  }

  @Override
  public SlashCommandData getCommandData() {
    return Commands.slash(this.getCommandName(), this.getCommandDescription());
  }

  /**
   * Skips the current song.
   * 
   * @throws NullPointerException
   */
  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
      throws NullPointerException {
    if (!event.getName().equals(getCommandName())) {
      return;
    }

    try {

      if (!trackScheduler.isPlaying()) {
        throw new IllegalStateException("No song to skip!");
      }

      final TextChannel textChan = super.getTextChan(event);
      final Optional<Track> nextTrackOpt = this.trackScheduler
          .skipCurrentSong();

      MailMan.replyInteraction(event, new SongSkippedEmbed().getEmbed());

      if (nextTrackOpt.isEmpty()) {
        MailMan.send(Optional.of(textChan), new QueueEmptyEmbed().getEmbed());
      } else {
        MailMan.send(Optional.of(textChan),
            new CurrentSongEmbed(nextTrackOpt.get(), store).getEmbed());
      }

    } catch (InvalidChannelException | IllegalStateException exception) {
      MailMan.replyInteraction(event, new ExceptionEmbed(exception).getEmbed());
    }
  }
}
