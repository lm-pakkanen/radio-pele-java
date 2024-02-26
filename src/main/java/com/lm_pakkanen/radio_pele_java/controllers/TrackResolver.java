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
      boolean asPlaylist) throws FailedToLoadSongException {
    final List<String> finalUrls = new ArrayList<String>();

    if (url.contains("spotify")) {
      final List<String> qualifiedTrackNames = this.spotifyController
          .resolveQualifiedTrackNames(url);

      qualifiedTrackNames.stream().forEach(qualifiedTrackName -> {
        finalUrls.add("ytsearch:" + qualifiedTrackName);
      });

      asPlaylist = false;
    } else {
      finalUrls.add(url);
    }

    final TrackResolver.RapAudioLoadResultHandler resultHandler = new RapAudioLoadResultHandler(
        asPlaylist);

    for (final String finalUrl : finalUrls) {
      audioPlayerManager.loadItemSync(finalUrl, resultHandler);
    }

    final String failureMessage = resultHandler.getFailureMessage();;

    if (failureMessage != null) {
      throw new FailedToLoadSongException(failureMessage);
    }

    final List<AudioTrack> resolvedTracks = new ArrayList<>();
    resolvedTracks.addAll(resultHandler.getResolvedTracks());

    if (resolvedTracks.size() == 0) {
      throw new FailedToLoadSongException("Not found.");
    }

    return resolvedTracks;
  }

  /**
   * Handles the result of the audio load.
   */
  public class RapAudioLoadResultHandler implements AudioLoadResultHandler {

    final private boolean asPlaylist;
    private boolean isReady = false;
    private final @NonNull List<AudioTrack> resolvedTracks = new ArrayList<>();
    private @Nullable String failureMessage;

    public RapAudioLoadResultHandler(boolean asPlaylist) {
      this.asPlaylist = asPlaylist;
    }

    /**
     * Resolves track.
     */
    @Override
    public void trackLoaded(AudioTrack track) {
      this.resolvedTracks.add(track);
      this.isReady = true;
    }

    /**
     * Resolves playlist to it's selected track.
     */
    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

      if (playlist.isSearchResult()) {
        this.resolvedTracks.add(playlist.getTracks().get(0));
      } else if (this.asPlaylist) {
        this.resolvedTracks.addAll(playlist.getTracks());
      } else {
        AudioTrack audioTrack = playlist.getSelectedTrack();

        if (audioTrack == null) {
          audioTrack = playlist.getTracks().get(0);
        }

        this.resolvedTracks.add(audioTrack);
      }

      this.isReady = true;
    }

    /**
     * Sets failure message.
     */
    @Override
    public void noMatches() {
      if (this.asPlaylist) {
        this.failureMessage = "Playlist not found.";
      } else {
        this.failureMessage = "Song not found.";
      }

      this.isReady = true;
    }

    /**
     * Sets failure message.
     */
    @Override
    public void loadFailed(FriendlyException exception) {
      exception.printStackTrace();

      final StringBuilder failureMessageBuilder = new StringBuilder();

      failureMessageBuilder.append(exception.getMessage());
      failureMessageBuilder.append(".");

      if (this.asPlaylist) {
        failureMessageBuilder.insert(0, "Could not load playlist: ");
      } else {
        failureMessageBuilder.insert(0, "Could not load song: ");
      }

      this.failureMessage = failureMessageBuilder.toString();
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
