package com.lm_pakkanen.radio_pele_java.controllers;

import org.springframework.lang.NonNull;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class RapAudioEventHandler extends AudioEventAdapter {
  private final @NonNull TrackScheduler trackScheduler;

  public RapAudioEventHandler(@NonNull TrackScheduler trackScheduler) {
    this.trackScheduler = trackScheduler;
  }

  /**
   * Wrap 'onTrackEnd' handler here and use TrackScheduler's instance method.
   */
  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track,
      AudioTrackEndReason endReason) throws NullPointerException {
    if (player == null) {
      throw new NullPointerException("Player is null");
    }

    if (track == null) {
      throw new NullPointerException("Track is null");
    }

    this.trackScheduler.onTrackEndHandler(player, track, endReason);
  }
}
