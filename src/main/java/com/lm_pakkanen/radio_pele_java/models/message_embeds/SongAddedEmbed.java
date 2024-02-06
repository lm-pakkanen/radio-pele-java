package com.lm_pakkanen.radio_pele_java.models.message_embeds;

import org.springframework.lang.NonNull;

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder;
import com.lm_pakkanen.radio_pele_java.models.Store;
import com.lm_pakkanen.radio_pele_java.models.TrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public final class SongAddedEmbed implements IEmbedBuilder {
  private final @NonNull MessageEmbed embed;

  /**
   * @param track instance.
   * @param store instance.
   */
  public SongAddedEmbed(@NonNull AudioTrack track, @NonNull Store store) {

    final TrackInfo trackInfo = new TrackInfo(track);

    final StringBuilder trackTitleWithDurationBuilder = new StringBuilder();
    trackTitleWithDurationBuilder.append(trackInfo.getQualifiedName());
    trackTitleWithDurationBuilder.append(" | ");
    trackTitleWithDurationBuilder.append(trackInfo.getDuration());

    final String trackTitleWithDuration = trackTitleWithDurationBuilder
        .toString();

    final String queueLengthDescription = String
        .format("%d song(s) in Q after current song", store.getQueueSize());

    final EmbedBuilder embedBuilder = IEmbedBuilder.getEmbedBuilder();
    embedBuilder.setTitle("SONG ADDED");
    embedBuilder.addField("SONG", trackTitleWithDuration, false);
    embedBuilder.addField("Q", queueLengthDescription, false);

    final MessageEmbed embed = embedBuilder.build();

    if (embed == null) {
      throw new NullPointerException("Embed is null");
    }

    this.embed = embed;
  }

  /**
   * @return message embed.
   */
  @Override
  public @NonNull MessageEmbed getEmbed() {
    return this.embed;
  }
}
