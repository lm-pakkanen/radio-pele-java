package com.lm_pakkanen.radio_pele_java.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import jakarta.annotation.Nonnull;

public final class Store {
  private final @Nonnull List<AudioTrack> playListUrlsQueue = new ArrayList<AudioTrack>(
      50);
  private final @NonNull BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<AudioTrack>();

  /**
   * Adds a track to the queue.
   * 
   * @param track to add.
   */
  public void add(AudioTrack track) {
    this.queue.add(track);
  }

  /**
   * Adds a list of tracks to the queue.
   * 
   * @param urls to add.
   */
  public void addPlaylist(List<AudioTrack> audioTracks) {
    final int playlistSize = Math.min(50, audioTracks.size());
    final List<AudioTrack> playlistTracks = audioTracks.subList(0,
        playlistSize);
    this.playListUrlsQueue.clear();
    this.playListUrlsQueue.addAll(playlistTracks);
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
   * Gets and removes the first track from the queue.
   * 
   * @param async whether to wait for the track to be available.
   * @return the first track from the queue or null if queue is empty.
   */
  public @Nullable AudioTrack shift(boolean async) {
    if (!async) {
      return this.shift();
    }

    try {
      return this.queue.take();
    } catch (InterruptedException exception) {
      return null;
    }
  }

  /**
   * Gets and removes the first track from the playlist queue.
   * 
   * @return the first track from the queue or null if queue is empty.
   */
  public @Nullable AudioTrack shiftPlaylist() {
    if (this.playListUrlsQueue.isEmpty()) {
      return null;
    }

    return this.playListUrlsQueue.remove(0);
  }

  /**
   * Gets and removes the first track from the playlist queue.
   * 
   * @param async whether to wait for the track to be available.
   * @return the first track from the queue or null if queue is empty.
   */
  public @Nullable AudioTrack shiftPlaylist(boolean async) {
    if (!async) {
      return this.shiftPlaylist();
    }

    if (this.playListUrlsQueue.isEmpty()) {
      return null;
    }

    return this.playListUrlsQueue.remove(0);
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
    this.playListUrlsQueue.clear();
  }

  /**
   * Shuffles the queue.
   */
  public void shuffle() {
    if (this.hasPlaylist()) {
      List<AudioTrack> shuffledUrls = new ArrayList<AudioTrack>(
          this.playListUrlsQueue);

      Collections.shuffle(shuffledUrls);

      this.playListUrlsQueue.clear();
      this.playListUrlsQueue.addAll(shuffledUrls);

      return;
    }

    List<AudioTrack> shuffledTracks = new ArrayList<AudioTrack>(this.queue);

    Collections.shuffle(shuffledTracks);

    this.queue.clear();
    this.queue.addAll(shuffledTracks);
  }

  /**
   * @return queue size.
   */
  public int getQueueSize() {
    return this.queue.size();
  }

  /**
   * @return whether the playlist queue is empty.
   */
  public boolean hasPlaylist() {
    return !this.playListUrlsQueue.isEmpty();
  }
}
