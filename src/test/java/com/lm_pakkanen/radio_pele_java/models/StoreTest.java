package com.lm_pakkanen.radio_pele_java.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class StoreTest {
  private static final AudioTrack audioTrackMock = mock(AudioTrack.class);

  private Store store;

  @BeforeEach
  public void setUp() {
    this.store = new Store();
  }

  @Test
  @DisplayName("Test adding a track to the queue")
  public void testAddingTrackToQ() {
    assertEquals(0, this.store.getQueueSize());
    assertEquals(0, this.store.getPlaylistQueueSize());

    this.store.add(Optional.of(StoreTest.audioTrackMock));

    assertEquals(1, this.store.getQueueSize());
    assertEquals(0, this.store.getPlaylistQueueSize());
  }

  @Test
  @DisplayName("Test adding a playlist to the queue")
  public void testAddingPlaylistToQ() {
    assertEquals(0, this.store.getQueueSize());
    assertEquals(0, this.store.getPlaylistQueueSize());

    final AudioTrack[] tracks = new AudioTrack[] {
        StoreTest.audioTrackMock, StoreTest.audioTrackMock
    };

    this.store.addPlaylist(tracks);

    assertEquals(0, this.store.getQueueSize());
    assertEquals(2, this.store.getPlaylistQueueSize());
  }

  @Test
  @DisplayName("Test adding a playlist to the queue when a playlist already exists in the queue")
  public void testAddingPlaylistToQWhenExistingPlaylist() {
    assertEquals(0, this.store.getQueueSize());
    assertEquals(0, this.store.getPlaylistQueueSize());

    final AudioTrack[] tracks = new AudioTrack[] {
        StoreTest.audioTrackMock
    };

    final AudioTrack[] tracks2 = new AudioTrack[] {
        StoreTest.audioTrackMock, StoreTest.audioTrackMock
    };

    this.store.addPlaylist(tracks);

    this.store.addPlaylist(tracks2);

    assertEquals(0, this.store.getQueueSize());
    assertEquals(2, this.store.getPlaylistQueueSize());
  }

  @Test
  @DisplayName("Test shift")
  public void testShift() {
    final Optional<AudioTrack> trackOpt = this.store.shift();
    this.store.add(Optional.of(StoreTest.audioTrackMock));
    final Optional<AudioTrack> track2Opt = this.store.shift();
    assertEquals(true, trackOpt.isEmpty());
    assertEquals(false, track2Opt.isEmpty());
  }

  @Test
  @DisplayName("Test shift playlist")
  public void testShiftPlaylist() {
    final Optional<AudioTrack> trackOpt = this.store.shiftPlaylist();
    this.store.addPlaylist(new AudioTrack[] {
        StoreTest.audioTrackMock
    });
    final Optional<AudioTrack> track2Opt = this.store.shiftPlaylist();
    assertEquals(true, trackOpt.isEmpty());
    assertEquals(true, track2Opt.isPresent());
  }

  @Test
  @DisplayName("Test clearing queue")
  public void testClearingQueue() {
    assertEquals(0, this.store.getQueueSize());
    this.store.add(Optional.of(StoreTest.audioTrackMock));
    assertEquals(1, this.store.getQueueSize());
    this.store.clear();
    assertEquals(0, this.store.getQueueSize());
  }

  @Test
  @DisplayName("Test clearing playlist queue")
  public void testClearingPlaylistQueue() {
    assertEquals(0, this.store.getPlaylistQueueSize());
    this.store.addPlaylist(new AudioTrack[] {
        StoreTest.audioTrackMock
    });
    assertEquals(1, this.store.getPlaylistQueueSize());
    this.store.clearPlaylist();
    assertEquals(0, this.store.getPlaylistQueueSize());
  }

  @Configuration
  public static class Config {}
}
