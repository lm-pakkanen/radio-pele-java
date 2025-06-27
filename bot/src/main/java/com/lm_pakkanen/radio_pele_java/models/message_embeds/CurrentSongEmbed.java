package com.lm_pakkanen.radio_pele_java.models.message_embeds;

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder;
import com.lm_pakkanen.radio_pele_java.models.Store;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
public final class CurrentSongEmbed implements IEmbedBuilder {

  private final MessageEmbed embed;

  /**
   * @param track instance.
   * @param store instance.
   */
  public CurrentSongEmbed(Track track, Store store) {

    final TrackInfo trackInfo = track.getInfo();

    final StringBuilder trackTitleWithDurationBuilder = new StringBuilder();
    trackTitleWithDurationBuilder.append(trackInfo.getSourceName());
    trackTitleWithDurationBuilder.append(" | ");
    trackTitleWithDurationBuilder.append(trackInfo.getLength());

    final String trackTitleWithDuration = trackTitleWithDurationBuilder
        .toString();

    String queueLengthDescription = null;

    if (store.hasPlaylist() && store.getQueueSize() == 0) {

      queueLengthDescription = String.format(
          "Playlist with %d song(s) in Q after current song",
          store.getPlaylistQueueSize());

    } else if (store.hasPlaylist()) {

      queueLengthDescription = String.format(
          "Q'd playlist will be destroyed after current song, %d song(s) in normal Q",
          store.getQueueSize());

    } else {

      queueLengthDescription = String
          .format("%d song(s) in Q after current song", store.getQueueSize());

    }

    final EmbedBuilder embedBuilder = IEmbedBuilder.getEmbedBuilder();
    embedBuilder.setTitle("NOW PLAYING");
    embedBuilder.addField("SONG", trackTitleWithDuration, false);
    embedBuilder.addField("Q", queueLengthDescription, false);
    this.embed = embedBuilder.build();
  }

}
