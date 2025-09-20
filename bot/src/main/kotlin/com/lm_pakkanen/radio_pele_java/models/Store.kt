package com.lm_pakkanen.radio_pele_java.models

import com.lm_pakkanen.radio_pele_java.Config
import dev.arbjerg.lavalink.client.player.Track
import lombok.extern.log4j.Log4j2
import org.springframework.stereotype.Component
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
     * @param track to add.
     */
    fun add(track: Track?): Boolean {
        var wasPresent = false

        track?.let { track -> wasPresent = this.queue.offer(track) }

        return wasPresent
    }

    /**
     * Adds a list of tracks to the queue.
     *
     * @param audioTracks to add.
     */
    fun addPlaylist(audioTracks: List<Track>) {
        val playlistSize: Int =
            min(
                Config.PLAYLIST_MAX_SIZE,
                audioTracks.size,
            )

        this.playListQueue.clear()
        this.playListQueue.addAll(audioTracks.subList(0, playlistSize))
    }

    /**
     * Gets and removes the first track from the queue.
     *
     * @return the first track from the queue or null if queue is empty.
     */
    fun shift(): Track? = queue.poll()

    /**
     * Gets and removes the first track from the playlist queue.
     *
     * @return the first track from the queue or null if queue is empty.
     */
    fun shiftPlaylist(): Track? {
        if (this.playListQueue.isEmpty()) {
            return null
        }

        return this.playListQueue.removeAt(0)
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
        if (this.hasPlaylist()) {
            shufflePlaylist()
            return
        }

        val shuffledTracks: MutableList<Track> = ArrayList(this.queue)
        shuffledTracks.shuffle()
        this.queue.clear()
        this.queue.addAll(shuffledTracks)
    }

    fun getQueueSize(): Int = this.queue.size

    fun getPlaylistQueueSize(): Int = this.playListQueue.size

    fun hasPlaylist(): Boolean = !this.playListQueue.isEmpty()

    /**
     * Shuffles the playlist queue.
     */
    private fun shufflePlaylist() {
    val shuffledUrls: MutableList<Track> = ArrayList(this.playListQueue)
    shuffledUrls.shuffle()
    this.playListQueue.clear()
    this.playListQueue.addAll(shuffledUrls)
  }
}
