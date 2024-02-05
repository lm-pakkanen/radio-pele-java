package com.lm_pakkanen.radio_pele_java.controllers;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class TrackResolver {
  private final AudioPlayerManager audioPlayerManager;

  public TrackResolver(AudioPlayerManager audioPlayerManager) {
    this.audioPlayerManager = audioPlayerManager;
  }

  public @NonNull AudioTrack resolve(String url)
      throws FailedToLoadSongException {
    String finalUrl = url;

    if (url.startsWith("spotify")) {
      finalUrl = url; // TODO
    }

    final TrackResolver.RapAudioLoadResultHandler resultHandler = this.new RapAudioLoadResultHandler();

    audioPlayerManager.loadItemSync(finalUrl, resultHandler);

    AudioTrack resolvedTrack = resultHandler.getResolvedTrack();
    String failureMessage = resultHandler.getFailureMessage();

    if (resolvedTrack == null && failureMessage != null) {
      throw new FailedToLoadSongException(failureMessage);
    } else if (resolvedTrack == null) {
      throw new FailedToLoadSongException();
    }

    return resolvedTrack;
  }

  public class RapAudioLoadResultHandler implements AudioLoadResultHandler {
    private boolean isReady = false;
    private @Nullable AudioTrack resolvedTrack;
    private @Nullable String failureMessage;

    public RapAudioLoadResultHandler() {}

    @Override
    public void trackLoaded(AudioTrack track) {
      this.resolvedTrack = track;
      this.isReady = true;
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
      AudioTrack selectedTrack = playlist.getSelectedTrack();
      this.resolvedTrack = selectedTrack;
      this.isReady = true;
    }

    @Override
    public void noMatches() {
      this.failureMessage = "Song not found.";
      this.isReady = true;
    }

    @Override
    public void loadFailed(FriendlyException exception) {
      exception.printStackTrace();
      this.failureMessage = "Could not load track.";
      this.isReady = true;
    }

    public boolean getIsReady() {
      return this.isReady;
    }

    public @Nullable AudioTrack getResolvedTrack() {
      return this.resolvedTrack;
    }

    public @Nullable String getFailureMessage() {
      return this.failureMessage;
    }
  }
}
