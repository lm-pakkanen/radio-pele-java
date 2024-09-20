package com.lm_pakkanen.radio_pele_java.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.Config;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@Lazy
public final class Store {
  private final List<AudioTrack> playListQueue = new ArrayList<>(1);
  private final BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();

  /**
   * Adds a track to the queue.
   * 
   * @param track to add.
   */
  public boolean add(Optional<AudioTrack> track) {
    if (track.isEmpty()) {
      return false;
    }

    return this.queue.offer(track.get());
  }

  /**
   * Adds a list of tracks to the queue.
   * 
   * @param urls to add.
   */
  public void addPlaylist(AudioTrack[] audioTracks) {
    final int playlistSize = Math.min(Config.PLAYLIST_MAX_SIZE,
        audioTracks.length);

    this.playListQueue.clear();
    this.playListQueue
        .addAll(Arrays.asList(audioTracks).subList(0, playlistSize));
  }

  /**
   * Gets and removes the first track from the queue.
   * 
   * @return the first track from the queue or null if queue is empty.
   */
  public Optional<AudioTrack> shift() {
    log.info("Shifting queue");
    Optional<AudioTrack> trackOpt = Optional.ofNullable(queue.poll());

    if (trackOpt.isPresent()) {
      AudioTrackInfo trackInfo = trackOpt.get().getInfo();
      String title = trackInfo == null ? "Unknown title" : trackInfo.title;
      log.debug("Shited queue, found track: '{}'", title);
    } else {
      log.debug("Track not found");
    }

    return trackOpt;
  }

  /**
   * Gets and removes the first track from the playlist queue.
   * 
   * @return the first track from the queue or null if queue is empty.
   */
  public Optional<AudioTrack> shiftPlaylist() {
    if (this.playListQueue.isEmpty()) {
      return Optional.empty();
    }

    return Optional.ofNullable(this.playListQueue.remove(0));
  }

  /**
   * Clears the queue.
   */
  public void clear() {
    this.queue.clear();
  }

  /**
   * Clears the queue.
   */
  public void clearPlaylist() {
    this.playListQueue.clear();
  }

  /**
   * Shuffles the queue.
   */
  public void shuffle() {
    final List<AudioTrack> shuffledTracks = new ArrayList<>(this.queue);
    Collections.shuffle(shuffledTracks);
    this.queue.clear();
    this.queue.addAll(shuffledTracks);
  }

  /**
   * Shuffles the playlist queue.
   */
  public void shufflePlaylist() {
    if (!this.hasPlaylist()) {
      return;
    }

    final List<AudioTrack> shuffledUrls = new ArrayList<>(this.playListQueue);
    Collections.shuffle(shuffledUrls);
    this.playListQueue.clear();
    this.playListQueue.addAll(shuffledUrls);

  }

  /**
   * @return queue size.
   */
  public int getQueueSize() {
    return this.queue.size();
  }

  /**
   * @return playlist queue size.
   */
  public int getPlaylistQueueSize() {
    return this.playListQueue.size();
  }

  /**
   * @return whether the playlist queue is empty.
   */
  public boolean hasPlaylist() {
    return !this.playListQueue.isEmpty();
  }
}
