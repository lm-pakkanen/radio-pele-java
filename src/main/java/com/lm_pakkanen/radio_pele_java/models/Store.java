package com.lm_pakkanen.radio_pele_java.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class Store {
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
   * Clears the queue.
   */
  public void clear() {
    this.queue.clear();
  }

  /**
   * Shuffles the queue.
   */
  public void shuffle() {
    final List<AudioTrack> shuffledTracks = new ArrayList<AudioTrack>(
        this.queue);

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
}
