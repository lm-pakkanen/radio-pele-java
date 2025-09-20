package com.lmpakkanen.radiopelejava.controllers

import com.lmpakkanen.radiopelejava.models.Store
import com.lmpakkanen.radiopelejava.models.exceptions.FailedToLoadSongException
import com.lmpakkanen.radiopelejava.models.message_embeds.CurrentSongEmbed
import com.lmpakkanen.radiopelejava.models.message_embeds.QueueEmptyEmbed
import com.lmpakkanen.radiopelejava.util.LavaLinkUtil.Companion.getPlayer
import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

val logger = LoggerFactory.getLogger(TrackScheduler::class.java)

@Component
class TrackScheduler(
    lavalinkClient: LavalinkClient,
    tidalController: TidalController,
    spotifyController: SpotifyController,
    private val store: Store,
) {
    private val trackResolver: TrackResolver =
        TrackResolver(
            tidalController,
            spotifyController,
        )

    init {
        lavalinkClient
            .on(TrackEndEvent::class.java)
            .subscribe { event -> onTrackEndHandler(event.endReason) }
    }

    companion object {
        private val PLAYLIST_URI_MATCHERS: List<String> = listOf("/playlist/", "/album/", "?list=", "&list=")
    }

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
        blockPlaylists: Boolean,
    ): Track {
        if (textChan != null) {
            this.lastTextChan = textChan
        } else {
            logger.warn("Text channel is null, cannot set lastTextChan.")
        }

        this.guildId = guildId

        if (url.isNullOrEmpty()) {
            throw FailedToLoadSongException("Invalid url.")
        }

        val asPlaylist =
            !blockPlaylists &&
                PLAYLIST_URI_MATCHERS.stream().anyMatch { s -> url.contains(s) }

        val audioTracks =
            this.trackResolver.resolve(
                guildId,
                url,
                asPlaylist,
            )

        if (audioTracks.isEmpty()) {
            throw FailedToLoadSongException("Not found.")
        }

        val firstTrack: Track = audioTracks.first()

        if (asPlaylist) {
            this.store.addPlaylist(audioTracks)
        } else {
            val addResult = this.store.add(firstTrack)

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
    fun skipCurrentSong(): Track? {
        stopCurrentSong()
        return this.playNextTrack()
    }

    /**
     * Stops the audio player and clears the queue. Sets the last text channel to
     * null.
     *
     */
    fun destroy() {
        try {
            stopCurrentSong()
        } catch (ex: Exception) {
            logger.warn("Failed to stop current song", ex)
        } finally {
            this.store.clear()
            this.store.clearPlaylist()
            this.lastTextChan = null
        }
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

        val nextTrack = this.playNextTrack()

        val textChannel =
            this.lastTextChan
                ?: throw IllegalStateException("Last text chan is not available.")

        if (nextTrack == null) {
            MailMan.send(
                textChannel,
                QueueEmptyEmbed().embed,
            )
            return
        }

        MailMan.send(
            textChannel,
            CurrentSongEmbed(nextTrack, this.store).embed,
        )
    }

    /**
     * Plays the next track in the queue if available.
     */
    private fun playNextTrack(): Track? {
        if (this.store.getQueueSize() > 0 || !this.store.hasPlaylist()) {
            if (this.store.hasPlaylist()) {
                this.store.clearPlaylist()
            }

            val nextTrack = this.store.shift()

            nextTrack?.let { nextTrack ->
                getPlayer(guildId).setTrack(nextTrack).subscribe()
            } ?: run {
                logger.warn("No next track available in the queue.")
            }

            return nextTrack
        }

        val nextTrack = this.store.shiftPlaylist()

        nextTrack?.let { nextTrack ->
            getPlayer(guildId).setTrack(nextTrack).subscribe()
        } ?: run {
            logger.warn("No next track available in the playlist.")
        }

        return nextTrack
    }

    private fun stopCurrentSong() {
        getPlayer(guildId).setPaused(false).setTrack(null).subscribe()
    }
}
