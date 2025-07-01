package com.lm_pakkanen.radio_pele_java.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import dev.arbjerg.lavalink.client.player.Track;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class StoreTest {

  private static final Track audioTrackMock = mock(Track.class);

  private Store store;

  @BeforeEach
  void setUp() {
    this.store = new Store();
  }

  @Test
  @DisplayName("Test adding a track to the queue")
  void testAddingTrackToQ() {
    assertEquals(0, this.store.getQueueSize());
    assertEquals(0, this.store.getPlaylistQueueSize());

    this.store.add(Optional.of(StoreTest.audioTrackMock));

    assertEquals(1, this.store.getQueueSize());
    assertEquals(0, this.store.getPlaylistQueueSize());
  }

  @Test
  @DisplayName("Test adding a playlist to the queue")
  void testAddingPlaylistToQ() {
    assertEquals(0, this.store.getQueueSize());
    assertEquals(0, this.store.getPlaylistQueueSize());

    final List<Track> tracks = List.of(StoreTest.audioTrackMock,
        StoreTest.audioTrackMock);

    this.store.addPlaylist(tracks);

    assertEquals(0, this.store.getQueueSize());
    assertEquals(2, this.store.getPlaylistQueueSize());
  }

  @Test
  @DisplayName("Test adding a playlist to the queue when a playlist already exists in the queue")
  void testAddingPlaylistToQWhenExistingPlaylist() {
    assertEquals(0, this.store.getQueueSize());
    assertEquals(0, this.store.getPlaylistQueueSize());

    final List<Track> tracks = List.of(StoreTest.audioTrackMock);

    final List<Track> tracks2 = List.of(StoreTest.audioTrackMock,
        StoreTest.audioTrackMock);

    this.store.addPlaylist(tracks);
    this.store.addPlaylist(tracks2);

    assertEquals(0, this.store.getQueueSize());
    assertEquals(2, this.store.getPlaylistQueueSize());
  }

  @Test
  @DisplayName("Test shift")
  void testShift() {
    final Optional<Track> trackOpt = this.store.shift();
    this.store.add(Optional.of(StoreTest.audioTrackMock));
    final Optional<Track> track2Opt = this.store.shift();
    assertEquals(true, trackOpt.isEmpty());
    assertEquals(false, track2Opt.isEmpty());
  }

  @Test
  @DisplayName("Test shift playlist")
  void testShiftPlaylist() {
    final Optional<Track> trackOpt = this.store.shiftPlaylist();
    this.store.addPlaylist(List.of(StoreTest.audioTrackMock));
    final Optional<Track> track2Opt = this.store.shiftPlaylist();
    assertEquals(true, trackOpt.isEmpty());
    assertEquals(true, track2Opt.isPresent());
  }

  @Test
  @DisplayName("Test clearing queue")
  void testClearingQueue() {
    assertEquals(0, this.store.getQueueSize());
    this.store.add(Optional.of(StoreTest.audioTrackMock));
    assertEquals(1, this.store.getQueueSize());
    this.store.clear();
    assertEquals(0, this.store.getQueueSize());
  }

  @Test
  @DisplayName("Test clearing playlist queue")
  void testClearingPlaylistQueue() {
    assertEquals(0, this.store.getPlaylistQueueSize());
    this.store.addPlaylist(List.of(StoreTest.audioTrackMock));
    assertEquals(1, this.store.getPlaylistQueueSize());
    this.store.clearPlaylist();
    assertEquals(0, this.store.getPlaylistQueueSize());
  }

  @Configuration
  public static class Config {}
}
