package com.lm_pakkanen.radio_pele_java.models.message_embeds

import com.lm_pakkanen.radio_pele_java.interfaces.IEmbedBuilder
import com.lm_pakkanen.radio_pele_java.models.Store
import dev.arbjerg.lavalink.client.player.Track
import net.dv8tion.jda.api.entities.MessageEmbed

class CurrentSongEmbed(track: Track, store: Store) : IEmbedBuilder {

  override val embed: MessageEmbed

  init {
    val trackInfo = track.info

    val trackTitleWithDurationBuilder = StringBuilder()
    trackTitleWithDurationBuilder.append(trackInfo.sourceName)
    trackTitleWithDurationBuilder.append(" | ")
    trackTitleWithDurationBuilder.append(trackInfo.length)

    val trackTitleWithDuration = trackTitleWithDurationBuilder
      .toString()

    val queueSize = store.getQueueSize()
    val playlistQueueSize = store.getPlaylistQueueSize()

    val queueLengthDescription = if (store.hasPlaylist() && queueSize == 0) {
      "Playlist with $playlistQueueSize song(s) in Q after current song"
    } else if (store.hasPlaylist()) {
      "Q'd playlist will be destroyed after current song, $queueSize song(s) in normal Q"
    } else {
      "$queueSize song(s) in Q after current song"
    }

    val embedBuilder = IEmbedBuilder.getEmbedBuilder()
    embedBuilder.setTitle("NOW PLAYING")
    embedBuilder.addField("SONG", trackTitleWithDuration, false)
    embedBuilder.addField("Q", queueLengthDescription, false)
    this.embed = embedBuilder.build()
  }
}
