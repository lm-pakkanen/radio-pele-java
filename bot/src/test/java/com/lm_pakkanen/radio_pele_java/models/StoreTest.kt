package com.lm_pakkanen.radio_pele_java.models

import dev.arbjerg.lavalink.client.player.Track
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
internal class StoreTest {

  private var store: Store? = null

  @BeforeEach
  fun setUp() {
    this.store = Store()
  }

  @Test
  @DisplayName("Test adding a track to the queue")
  fun testAddingTrackToQ() {
    Assertions.assertEquals(0, this.store!!.queueSize)
    Assertions.assertEquals(0, this.store!!.playlistQueueSize)

    this.store!!.add(Optional.of(audioTrackMock))

    Assertions.assertEquals(1, this.store!!.queueSize)
    Assertions.assertEquals(0, this.store!!.playlistQueueSize)
  }

  @Test
  @DisplayName("Test adding a playlist to the queue")
  fun testAddingPlaylistToQ() {
    Assertions.assertEquals(0, this.store!!.queueSize)
    Assertions.assertEquals(0, this.store!!.playlistQueueSize)

    val tracks = listOf(
      audioTrackMock,
      audioTrackMock
    )

    this.store!!.addPlaylist(tracks)

    Assertions.assertEquals(0, this.store!!.queueSize)
    Assertions.assertEquals(2, this.store!!.playlistQueueSize)
  }

  @Test
  @DisplayName("Test adding a playlist to the queue when a playlist already exists in the queue")
  fun testAddingPlaylistToQWhenExistingPlaylist() {
    Assertions.assertEquals(0, this.store!!.queueSize)
    Assertions.assertEquals(0, this.store!!.playlistQueueSize)

    val tracks = listOf(audioTrackMock)
    val tracks2 = listOf(audioTrackMock, audioTrackMock)

    this.store!!.addPlaylist(tracks)
    this.store!!.addPlaylist(tracks2)

    Assertions.assertEquals(0, this.store!!.queueSize)
    Assertions.assertEquals(2, this.store!!.playlistQueueSize)
  }

  @Test
  @DisplayName("Test shift")
  fun testShift() {
    val trackOpt = this.store!!.shift()
    this.store!!.add(Optional.of(audioTrackMock))
    val track2Opt = this.store!!.shift()
    Assertions.assertEquals(true, trackOpt.isEmpty)
    Assertions.assertEquals(false, track2Opt.isEmpty)
  }

  @Test
  @DisplayName("Test shift playlist")
  fun testShiftPlaylist() {
    val trackOpt = this.store!!.shiftPlaylist()
    this.store!!.addPlaylist(listOf(audioTrackMock))
    val track2Opt = this.store!!.shiftPlaylist()
    Assertions.assertEquals(true, trackOpt.isEmpty)
    Assertions.assertEquals(true, track2Opt.isPresent)
  }

  @Test
  @DisplayName("Test clearing queue")
  fun testClearingQueue() {
    Assertions.assertEquals(0, this.store!!.queueSize)
    this.store!!.add(Optional.of(audioTrackMock))
    Assertions.assertEquals(1, this.store!!.queueSize)
    this.store!!.clear()
    Assertions.assertEquals(0, this.store!!.queueSize)
  }

  @Test
  @DisplayName("Test clearing playlist queue")
  fun testClearingPlaylistQueue() {
    Assertions.assertEquals(0, this.store!!.playlistQueueSize)
    this.store!!.addPlaylist(listOf(audioTrackMock))
    Assertions.assertEquals(1, this.store!!.playlistQueueSize)
    this.store!!.clearPlaylist()
    Assertions.assertEquals(0, this.store!!.playlistQueueSize)
  }

  @Configuration
  open class Config
  companion object {
    private val audioTrackMock: Track = Mockito.mock<Track>(Track::class.java)
  }
}
