package com.lm_pakkanen.radio_pele_java.models

import dev.arbjerg.lavalink.client.player.Track
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import java.util.List
import java.util.Optional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
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

    this.store.add(Optional.of<Track>(audioTrackMock))

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
    val trackOpt: Optional<Track> = this.store.shift()
    this.store.add(Optional.of<Track>(audioTrackMock))
    val track2Opt: Optional<Track> = this.store.shift()
    Assertions.assertEquals(true, trackOpt.isEmpty)
    Assertions.assertEquals(false, track2Opt.isEmpty)
  }

  @Test
  @DisplayName("Test shift playlist")
  fun testShiftPlaylist() {
    val trackOpt: Optional<Track> = this.store.shiftPlaylist()
    this.store.addPlaylist(List.of<Track>(audioTrackMock))
    val track2Opt: Optional<Track> = this.store.shiftPlaylist()
    Assertions.assertEquals(true, trackOpt.isEmpty)
    Assertions.assertEquals(true, track2Opt.isPresent)
  }

  @Test
  @DisplayName("Test clearing queue")
  fun testClearingQueue() {
    Assertions.assertEquals(0, this.store.getQueueSize())
    this.store.add(Optional.of(audioTrackMock))
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
