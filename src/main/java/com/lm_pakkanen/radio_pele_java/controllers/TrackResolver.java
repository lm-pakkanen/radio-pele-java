package com.lm_pakkanen.radio_pele_java.controllers;

import java.util.ArrayList;
import java.util.List;

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
  public @NonNull List<AudioTrack> resolve(@NonNull String url,
      boolean isPlaylist) throws FailedToLoadSongException {
    String finalUrl = url;

    if (url.contains("spotify")) {
      final String qualifiedTrackName = this.spotifyController
          .resolveQualifiedTrackName(url);

      finalUrl = "ytsearch:" + qualifiedTrackName;
    }

    final List<AudioTrack> resolvedTracks = new ArrayList<AudioTrack>();
    String failureMessage = null;

    if (isPlaylist) {
      final TrackResolver.RapPlaylistAudioLoadResultHandler resultHandler = this.new RapPlaylistAudioLoadResultHandler();
      audioPlayerManager.loadItemSync(finalUrl, resultHandler);
      resolvedTracks.addAll(resultHandler.getResolvedTracks());
      failureMessage = resultHandler.getFailureMessage();

    } else {
      final TrackResolver.RapAudioLoadResultHandler resultHandler = this.new RapAudioLoadResultHandler();
      audioPlayerManager.loadItemSync(finalUrl, resultHandler);
      resolvedTracks.add(resultHandler.getResolvedTrack());
      failureMessage = resultHandler.getFailureMessage();

    }

    if (resolvedTracks.size() == 0 && failureMessage != null) {
      throw new FailedToLoadSongException(failureMessage);
    } else if (resolvedTracks.size() == 0) {
      throw new FailedToLoadSongException("Not found.");
    }

    return resolvedTracks;
  }

  /**
   * Handles the result of the audio load.
   */
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

  /**
   * Handles the result of the audio load.
   */
  public class RapPlaylistAudioLoadResultHandler
      implements AudioLoadResultHandler {

    private boolean isReady = false;
    private @Nullable List<AudioTrack> resolvedTracks;
    private @Nullable String failureMessage;

    public RapPlaylistAudioLoadResultHandler() {}

    /**
     * Resolves track.
     */
    @Override
    public void trackLoaded(AudioTrack track) {
      this.resolvedTracks = new ArrayList<AudioTrack>();
      this.resolvedTracks.add(track);
      this.isReady = true;
    }

    /**
     * Resolves playlist to it's selected track.
     */
    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
      this.resolvedTracks = playlist.getTracks();
      this.isReady = true;
    }

    /**
     * Sets failure message.
     */
    @Override
    public void noMatches() {
      this.failureMessage = "Playlist not found.";
      this.isReady = true;
    }

    /**
     * Sets failure message.
     */
    @Override
    public void loadFailed(FriendlyException exception) {
      exception.printStackTrace();
      this.failureMessage = "Could not load playlist.";
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
    public @Nullable List<AudioTrack> getResolvedTracks() {
      return this.resolvedTracks;
    }

    /**
     * @return failure message. Null if no failure was encountered.
     */
    public @Nullable String getFailureMessage() {
      return this.failureMessage;
    }
  }
}
