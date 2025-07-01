package com.lm_pakkanen.radio_pele_java.models

import com.lm_pakkanen.radio_pele_java.Config
import dev.arbjerg.lavalink.client.player.Track
import lombok.extern.log4j.Log4j2
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.min

@Log4j2
@Component
class Store {

  val playListQueue: MutableList<Track> = ArrayList(1)
  val queue: BlockingQueue<Track> = LinkedBlockingQueue()

  /**
   * Adds a track to the queue.
   *
   * @param trackOpt to add.
   */
  fun add(trackOpt: Optional<Track>): Boolean {
    var wasPresent = false
    trackOpt.ifPresent({ track -> wasPresent = this.queue.offer(track) })
    return wasPresent
  }

  /**
   * Adds a list of tracks to the queue.
   *
   * @param audioTracks to add.
   */
  fun addPlaylist(audioTracks: List<Track>) {

    val playlistSize: Int = min(
      Config.PLAYLIST_MAX_SIZE,
      audioTracks.size
    )

    this.playListQueue.clear()
    this.playListQueue.addAll(audioTracks.subList(0, playlistSize))
  }

  /**
   * Gets and removes the first track from the queue.
   *
   * @return the first track from the queue or null if queue is empty.
   */
  fun shift(): Optional<Track> {

    val trackOpt: Optional<Track> = Optional.ofNullable(queue.poll())
    trackOpt.ifPresentOrElse({ track -> }, { })

    return trackOpt
  }

  /**
   * Gets and removes the first track from the playlist queue.
   *
   * @return the first track from the queue or null if queue is empty.
   */
  fun shiftPlaylist(): Optional<Track> {

    if (this.playListQueue.isEmpty()) {
      return Optional.empty()
    }

    return Optional.ofNullable(this.playListQueue.removeAt(0))
  }

  fun clear() {
    this.queue.clear()
  }

  fun clearPlaylist() {
    this.playListQueue.clear()
  }

  /**
   * Shuffles the queue.
   */
  fun shuffle() {
    val shuffledTracks: MutableList<Track> = ArrayList(this.queue)
    shuffledTracks.shuffle()
    this.queue.clear()
    this.queue.addAll(shuffledTracks)
  }

  /**
   * Shuffles the playlist queue.
   */
  fun shufflePlaylist() {

    if (!this.hasPlaylist()) {
      return
    }

    val shuffledUrls: MutableList<Track> = ArrayList(this.playListQueue)
    shuffledUrls.shuffle()
    this.playListQueue.clear()
    this.playListQueue.addAll(shuffledUrls)
  }

  fun getQueueSize(): Int {
    return this.queue.size
  }

  fun getPlaylistQueueSize(): Int {
    return this.playListQueue.size
  }

  fun hasPlaylist(): Boolean {
    return !this.playListQueue.isEmpty()
  }
}
