package com.lm_pakkanen.radio_pele_java.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class TrackResolver {
  private final @NonNull SpotifyController spotifyController;
  private final @NonNull AudioPlayerManager audioPlayerManager;

  public TrackResolver(@Autowired @NonNull SpotifyController spotifyController,
      @NonNull AudioPlayerManager audioPlayerManager) {
    this.spotifyController = spotifyController;
    this.audioPlayerManager = audioPlayerManager;
  }

  /**
   * Try to resolve a song from a given URL. If the track can't be found, throws
   * an exception. Uses the LavaPlayer library to resolve the track. Spotify API
   * is used to resolve Spotify URLs into track and artist names.
   * 
   * @param url to try to resolve.
   * @return resolved track.
   * @throws FailedToLoadSongException
   */
  public @NonNull AudioTrack resolve(@NonNull String url)
      throws FailedToLoadSongException {
    String finalUrl = url;

    if (url.contains("spotify")) {
      final String qualifiedTrackName = this.spotifyController
          .resolveQualifiedTrackName(url);

      finalUrl = "ytsearch:" + qualifiedTrackName;
    }

    final TrackResolver.RapAudioLoadResultHandler resultHandler = this.new RapAudioLoadResultHandler();

    audioPlayerManager.loadItemSync(finalUrl, resultHandler);

    final AudioTrack resolvedTrack = resultHandler.getResolvedTrack();
    final String failureMessage = resultHandler.getFailureMessage();

    if (resolvedTrack == null && failureMessage != null) {
      throw new FailedToLoadSongException(failureMessage);
    } else if (resolvedTrack == null) {
      throw new FailedToLoadSongException("Not found.");
    }

    return resolvedTrack;
  }

  public class RapAudioLoadResultHandler implements AudioLoadResultHandler {
    private boolean isReady = false;
    private @Nullable AudioTrack resolvedTrack;
    private @Nullable String failureMessage;

    public RapAudioLoadResultHandler() {}

    /**
     * Resolves track.
     */
    @Override
    public void trackLoaded(AudioTrack track) {
      this.resolvedTrack = track;
      this.isReady = true;
    }

    /**
     * Resolves playlist to it's selected track.
     */
    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
      AudioTrack selectedTrack = playlist.getSelectedTrack();

      if (selectedTrack == null) {
        selectedTrack = playlist.getTracks().get(0);
      }

      if (selectedTrack == null) {
        this.failureMessage = "No tracks found.";
      } else {
        this.resolvedTrack = selectedTrack;
      }

      this.isReady = true;
    }

    /**
     * Sets failure message.
     */
    @Override
    public void noMatches() {
      this.failureMessage = "Song not found.";
      this.isReady = true;
    }

    /**
     * Sets failure message.
     */
    @Override
    public void loadFailed(FriendlyException exception) {
      exception.printStackTrace();
      this.failureMessage = "Could not load track.";
      this.isReady = true;
    }

    /**
     * @return whether resolving is ready.
     */
    public boolean getIsReady() {
      return this.isReady;
    }

    /**
     * @return resolved track. Null if failure was encountered.
     */
    public @Nullable AudioTrack getResolvedTrack() {
      return this.resolvedTrack;
    }

    /**
     * @return failure message. Null if no failure was encountered.
     */
    public @Nullable String getFailureMessage() {
      return this.failureMessage;
    }
  }
}
