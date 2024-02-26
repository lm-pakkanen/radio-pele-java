package com.lm_pakkanen.radio_pele_java.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import jakarta.annotation.Nonnull;

@Component
public final class Store {
  private final @Nonnull List<AudioTrack> playListQueue = new ArrayList<>(1);
  private final @NonNull BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();

  /**
   * Adds a track to the queue.
   * 
   * @param track to add.
   */
  public boolean add(@Nullable AudioTrack track) {
    return this.queue.offer(track);
  }

  /**
   * Adds a list of tracks to the queue.
   * 
   * @param urls to add.
   */
  public void addPlaylist(@NonNull AudioTrack[] audioTracks) {
    final int playlistSize = Math.min(15, audioTracks.length);

    this.playListQueue.clear();
    this.playListQueue
        .addAll(Arrays.asList(audioTracks).subList(0, playlistSize));
  }

  /**
   * Gets and removes the first track from the queue.
   * 
   * @return the first track from the queue or null if queue is empty.
   */
  public @Nullable AudioTrack shift() {
    return this.queue.poll();
  }

  /**
   * Gets and removes the first track from the playlist queue.
   * 
   * @return the first track from the queue or null if queue is empty.
   */
  public @Nullable AudioTrack shiftPlaylist() {
    if (this.playListQueue.isEmpty()) {
      return null;
    }

    return this.playListQueue.remove(0);
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
