package com.lm_pakkanen.radio_pele_java.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.lang.Nullable;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class Store {
  private BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<AudioTrack>();

  public void add(AudioTrack track) {
    this.queue.add(track);
  }

  public @Nullable AudioTrack shift() {
    return this.queue.poll();
  }

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

  public void shuffle() {
    List<AudioTrack> shuffledTracks = new ArrayList<AudioTrack>(this.queue);
    Collections.shuffle(shuffledTracks);
    this.queue.clear();
    this.queue.addAll(shuffledTracks);
  }
}
