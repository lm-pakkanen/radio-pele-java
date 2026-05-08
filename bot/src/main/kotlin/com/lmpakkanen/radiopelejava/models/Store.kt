package com.lmpakkanen.radiopelejava.models

import com.lmpakkanen.radiopelejava.Config
import dev.arbjerg.lavalink.client.player.Track
import lombok.extern.log4j.Log4j2
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.min

@Log4j2
@Component
class Store {
    // Initialize with capacity hint matching PLAYLIST_MAX_SIZE (25) from Config
    // This reduces ArrayList re-allocations and memory fragmentation
    val playListQueue: MutableList<Track> = ArrayList(Config.PLAYLIST_MAX_SIZE)
    val queue: BlockingQueue<Track> = LinkedBlockingQueue(Config.QUEUE_MAX_SIZE) // Bounded queue to prevent memory bloat

    /**
     * Adds a track to the queue.
     *
     * @param track to add.
     */
    fun add(track: Track?): Boolean {
        track?.let { track -> return this.queue.offer(track) }
        return false
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

        // Convert to list, shuffle in-place, then rebuild queue to avoid memory bloat
        val tracks = this.queue.toList()  // Single collection
        this.queue.clear()
        this.queue.addAll(tracks.shuffled())  // shuffled() creates list once, not twice
    }

    fun getQueueSize(): Int = this.queue.size

    fun getPlaylistQueueSize(): Int = this.playListQueue.size

    fun hasPlaylist(): Boolean = this.playListQueue.isNotEmpty()

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
