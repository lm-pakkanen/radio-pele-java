package com.lmpakkanen.radiopelejava.models

import dev.arbjerg.lavalink.client.player.Track
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class StoreTest {
    companion object {
        private val audioTrackMock: Track = Mockito.mock<Track>(Track::class.java)
    }

    private lateinit var store: Store

    @BeforeEach
    fun setUp() {
        this.store = Store()
    }

    @Test
    @DisplayName("Test adding a track to the queue")
    fun testAddingTrackToQ() {
        Assertions.assertEquals(0, this.store.getQueueSize())
        Assertions.assertEquals(0, this.store.getPlaylistQueueSize())

        this.store.add(audioTrackMock)

        Assertions.assertEquals(1, this.store.getQueueSize())
        Assertions.assertEquals(0, this.store.getPlaylistQueueSize())
    }

    @Test
    @DisplayName("Test adding a playlist to the queue")
    fun testAddingPlaylistToQ() {
        Assertions.assertEquals(0, this.store.getQueueSize())
        Assertions.assertEquals(0, this.store.getPlaylistQueueSize())

        val tracks = listOf(audioTrackMock, audioTrackMock)
        this.store.addPlaylist(tracks)

        Assertions.assertEquals(0, this.store.getQueueSize())
        Assertions.assertEquals(2, this.store.getPlaylistQueueSize())
    }

    @Test
    @DisplayName("Test adding a playlist to the queue when a playlist already exists in the queue")
    fun testAddingPlaylistToQWhenExistingPlaylist() {
        Assertions.assertEquals(0, this.store.getQueueSize())
        Assertions.assertEquals(0, this.store.getPlaylistQueueSize())

        val tracks = listOf(audioTrackMock)
        val tracks2 = listOf(audioTrackMock, audioTrackMock)

        this.store.addPlaylist(tracks)
        this.store.addPlaylist(tracks2)

        Assertions.assertEquals(0, this.store.getQueueSize())
        Assertions.assertEquals(2, this.store.getPlaylistQueueSize())
    }

    @Test
    @DisplayName("Test shift")
    fun testShift() {
        val track: Track? = this.store.shift()
        this.store.add(audioTrackMock)
        val track2: Track? = this.store.shift()
        Assertions.assertEquals(true, track == null)
        Assertions.assertEquals(true, track2 != null)
    }

    @Test
    @DisplayName("Test shift playlist")
    fun testShiftPlaylist() {
        val trackOpt: Track? = this.store.shiftPlaylist()
        this.store.addPlaylist(listOf(audioTrackMock))
        val track2Opt: Track? = this.store.shiftPlaylist()
        Assertions.assertEquals(true, trackOpt == null)
        Assertions.assertEquals(true, track2Opt != null)
    }

    @Test
    @DisplayName("Test clearing queue")
    fun testClearingQueue() {
        Assertions.assertEquals(0, this.store.getQueueSize())
        this.store.add(audioTrackMock)
        Assertions.assertEquals(1, this.store.getQueueSize())
        this.store.clear()
        Assertions.assertEquals(0, this.store.getQueueSize())
    }

    @Test
    @DisplayName("Test clearing playlist queue")
    fun testClearingPlaylistQueue() {
        Assertions.assertEquals(0, this.store.getPlaylistQueueSize())
        this.store.addPlaylist(listOf(audioTrackMock))
        Assertions.assertEquals(1, this.store.getPlaylistQueueSize())
        this.store.clearPlaylist()
        Assertions.assertEquals(0, this.store.getPlaylistQueueSize())
    }
}
