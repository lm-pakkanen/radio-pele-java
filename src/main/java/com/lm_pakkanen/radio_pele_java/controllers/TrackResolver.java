package com.lm_pakkanen.radio_pele_java.controllers;

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
  public @NonNull AudioTrack[] resolve(@NonNull String url, boolean asPlaylist)
      throws FailedToLoadSongException {
    final int capacity = asPlaylist ? 15 : 1;
    final String[] finalUrls = new String[capacity];

    if (url.contains("spotify")) {
      final String[] qualifiedTrackNames = this.spotifyController
          .resolveQualifiedTrackNames(url);

      for (int i = 0; i < qualifiedTrackNames.length; i++) {
        finalUrls[i] = "ytsearch:" + qualifiedTrackNames[i];
      }

      asPlaylist = false;
    } else {
      finalUrls[0] = url;
    }

    final TrackResolver.RapAudioLoadResultHandler resultHandler = new RapAudioLoadResultHandler(
        asPlaylist);

    int count = 0;

    for (final String finalUrl : finalUrls) {
      if (finalUrl == null) {
        break;
      }

      count++;
      audioPlayerManager.loadItemSync(finalUrl, resultHandler);
    }

    if (count == 0) {
      throw new FailedToLoadSongException("Not found.");
    }

    final String failureMessage = resultHandler.getFailureMessage();;

    if (failureMessage != null) {
      throw new FailedToLoadSongException(failureMessage);
    }

    final AudioTrack[] resolvedTracks = resultHandler.getResolvedTracks();

    return resolvedTracks;
  }

  /**
   * Handles the result of the audio load.
   */
  public class RapAudioLoadResultHandler implements AudioLoadResultHandler {

    private final boolean asPlaylist;
    private @NonNull AudioTrack[] resolvedTracks;

    private boolean isReady = false;
    private @Nullable String failureMessage;

    public RapAudioLoadResultHandler(boolean asPlaylist) {
      this.asPlaylist = asPlaylist;
      int initialCap = this.asPlaylist ? 15 : 1;
      this.resolvedTracks = new AudioTrack[initialCap];
    }

    /**
     * Resolves track.
     */
    @Override
    public void trackLoaded(AudioTrack track) {
      this.resolvedTracks[0] = track;
      this.isReady = true;
    }

    /**
     * Resolves playlist to it's selected track.
     */
    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

      if (playlist.isSearchResult()) {
        this.resolvedTracks[0] = playlist.getTracks().get(0);
      } else if (this.asPlaylist) {
        final List<AudioTrack> audioTracks = playlist.getTracks();

        final AudioTrack[] audioTracksArray = audioTracks
            .toArray(new AudioTrack[0]);

        if (audioTracksArray == null) {
          throw new NullPointerException();
        }

        this.resolvedTracks = audioTracksArray;
      } else {
        AudioTrack audioTrack = playlist.getSelectedTrack();

        if (audioTrack == null) {
          audioTrack = playlist.getTracks().get(0);
        }

        this.resolvedTracks[0] = audioTrack;
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
    public @NonNull AudioTrack[] getResolvedTracks() {
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
