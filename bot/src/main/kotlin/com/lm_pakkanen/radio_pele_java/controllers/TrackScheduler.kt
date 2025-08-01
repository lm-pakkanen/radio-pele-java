package com.lm_pakkanen.radio_pele_java.controllers

import com.lm_pakkanen.radio_pele_java.models.Store
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException
import com.lm_pakkanen.radio_pele_java.models.message_embeds.CurrentSongEmbed
import com.lm_pakkanen.radio_pele_java.models.message_embeds.QueueEmptyEmbed
import com.lm_pakkanen.radio_pele_java.util.LavaLinkUtil.Companion.getPlayer
import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.function.Consumer

@Component
class TrackScheduler(
  lavalinkClient: LavalinkClient,
  private val store: Store,
  private val tidalController: TidalController,
  private val spotifyController: SpotifyController
) {

  init {
    lavalinkClient
      .on(TrackEndEvent::class.java)
      .subscribe { event -> onTrackEndHandler(event.endReason) }
  }

  companion object {
    private val PLAYLIST_URI_MATCHERS: List<String> = listOf("/playlist/", "/album/", "?list=", "&list=")
  }

  private val trackResolver: TrackResolver = TrackResolver(
    this.tidalController,
    this.spotifyController
  )

  private var guildId: Long? = null
  private var lastTextChan: TextChannel? = null

  val isPlaying: Boolean
    get() = getPlayer(guildId).track != null

  /**
   * Start the audio player.
   *
   * @return boolean whether the action succeeded.
   */
  fun play(): Boolean {

    if (!this.isPlaying) {
      // If not yet playing a song, play the next one in the queue
      this.playNextTrack()
      return true
    }

    return false
  }

  /**
   * Tries to add the given URL to the queue. If the action fails, throws a
   * FailedToLoadSongException.
   *
   * @param textChan       TextChannel instance where the command was invoked.
   * @param url            the URL of the song to add to the queue.
   * @param blockPlaylists
   * @return boolean whether the action succeeded.
   * @throws FailedToLoadSongException
   */
  @Throws(FailedToLoadSongException::class)
  fun addToQueue(
    textChan: TextChannel?,
    guildId: Long,
    url: String?,
    blockPlaylists: Boolean
  ): Track {

    if (textChan != null) {
      this.lastTextChan = textChan
    } else {
      // TODO log
    }

    this.guildId = guildId

    if (url == null || url.isEmpty()) {
      throw FailedToLoadSongException("Invalid url.")
    }

    val asPlaylist = !blockPlaylists
        && PLAYLIST_URI_MATCHERS.stream().anyMatch { s -> url.contains(s) }

    val audioTracks = this.trackResolver.resolve(
      guildId,
      url,
      asPlaylist
    )

    if (audioTracks.isEmpty()) {
      throw FailedToLoadSongException("Not found.")
    }

    val firstTrack: Track = audioTracks.first()

    if (asPlaylist) {
      this.store.addPlaylist(audioTracks)
    } else {
      val addResult = this.store.add(Optional.of(firstTrack))

      if (!addResult) {
        throw FailedToLoadSongException("Not found.")
      }
    }

    return firstTrack
  }

  /**
   * Skips the current song (stops the track and starts the next one in the
   * queue).
   *
   * @return boolean whether the action succeeded.
   */
  fun skipCurrentSong(): Optional<Track> {
    stopCurrentSong()
    return this.playNextTrack()
  }

  /**
   * Stops the audio player and clears the queue. Sets the last text channel to
   * null.
   *
   */
  fun destroy() {
    stopCurrentSong()
    this.store.clear()
    this.store.clearPlaylist()
    this.lastTextChan = null
  }

  /**
   * Shuffles the current queue.
   */
  fun shuffle() {
    this.store.shuffle()
  }

  /**
   * Handler for when a track ends. If the track ended normally, tries to start
   * the next track if the queue is not empty. Sends message to the latest text
   * channel informing users about the next song or about the queue being empty.
   *
   * @param endReason the reason the track ended. Supplied by superclass.
   */
  private fun onTrackEndHandler(endReason: AudioTrackEndReason) {

    if (endReason == AudioTrackEndReason.LOAD_FAILED) {
      return
    }

    if (!endReason.mayStartNext) {
      return
    }

    val nextTrackOpt = this.playNextTrack()

    val textChannel = this.lastTextChan
      ?: throw IllegalStateException("Last text chan is not available.")

    if (nextTrackOpt.isEmpty) {
      MailMan.send(
        Optional.of(textChannel),
        QueueEmptyEmbed().embed
      )
      return
    }

    val nextTrack = nextTrackOpt.get()

    if (this.lastTextChan != null) {
      MailMan.send(
        Optional.of(textChannel),
        CurrentSongEmbed(nextTrack, this.store).embed
      )
    } else {
      // TODO log
    }
  }

  /**
   * Plays the next track in the queue if available.
   */
  private fun playNextTrack(): Optional<Track> {

    if (this.store.getQueueSize() > 0 || !this.store.hasPlaylist()) {

      if (this.store.hasPlaylist()) {
        this.store.clearPlaylist()
      }

      val nextTrackOpt = this.store.shift()

      nextTrackOpt.ifPresentOrElse(Consumer { nextTrack ->
        getPlayer(guildId).setTrack(nextTrack).subscribe()
      }, Runnable {
        // TODO log
      })

      return nextTrackOpt
    }

    val nextTrackOpt = this.store.shiftPlaylist()

    nextTrackOpt.ifPresentOrElse(Consumer { nextTrack ->
      getPlayer(guildId).setTrack(nextTrack).subscribe()
    }, Runnable {
      // TODO log
    })

    return nextTrackOpt
  }

  private fun stopCurrentSong() {
    getPlayer(guildId).setPaused(false).setTrack(null).subscribe()
  }
}
