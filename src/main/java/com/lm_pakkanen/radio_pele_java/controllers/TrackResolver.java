package com.lm_pakkanen.radio_pele_java.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.lm_pakkanen.radio_pele_java.Config;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class TrackResolver {
  private final @NonNull TidalController tidalController;
  private final @NonNull SpotifyController spotifyController;
  private final @NonNull AudioPlayerManager audioPlayerManager;

  public TrackResolver(@Autowired @NonNull TidalController tidalController,
      @Autowired @NonNull SpotifyController spotifyController,
      @NonNull AudioPlayerManager audioPlayerManager) {
    this.tidalController = tidalController;
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
    final int capacity = asPlaylist ? Config.PLAYLIST_MAX_SIZE : 1;
    final List<String> finalUrls = new ArrayList<String>(capacity);

    URI uri = null;

    try {
      uri = new URI(url);
    } catch (URISyntaxException exception) {
      throw new FailedToLoadSongException("Invalid URL.");
    }

    final String uriDomain = uri.getHost();

    if (uriDomain.contains("spotify")) {
      final List<String> qualifiedTrackNames = this.spotifyController
          .resolveQualifiedTrackNames(url);

      qualifiedTrackNames.stream().map(n -> "ytsearch:" + n)
          .forEach(finalUrls::add);

      asPlaylist = false;
    } else if (uriDomain.contains("tidal")) {
      final String[] qualifiedTrackNames = this.tidalController
          .resolveQualifiedTrackNames(url);

      for (int i = 0; i < qualifiedTrackNames.length; i++) {
        finalUrls.add("ytsearch:" + qualifiedTrackNames[i]);
      }

      asPlaylist = false;
    } else {
      finalUrls.add(url);
    }

    if (finalUrls.size() == 0) {
      throw new FailedToLoadSongException("Not found.");
    }

    final List<TrackResolver.RapAudioLoadResultHandler> resultHandlers = new ArrayList<>();

    for (final String finalUrl : finalUrls) {
      final TrackResolver.RapAudioLoadResultHandler resultHandler = new RapAudioLoadResultHandler(
          asPlaylist);
      resultHandlers.add(resultHandler);
      audioPlayerManager.loadItem(finalUrl, resultHandler);
      awaitResult(resultHandler);
    }

    final AggregatedResult aggregatedResult = getAggregatedResult(
        resultHandlers);

    if (aggregatedResult.failureMessage != null) {
      throw new FailedToLoadSongException(aggregatedResult.failureMessage);
    }

    final AudioTrack[] resolvedTracks = aggregatedResult.audioTracks;
    return resolvedTracks;
  }

  /**
   * Waits for the result to be ready.
   * 
   * @param resultHandler to wait for.
   * @throws FailedToLoadSongException
   */
  private static void awaitResult(
      TrackResolver.RapAudioLoadResultHandler resultHandler)
      throws FailedToLoadSongException {
    final int MAX_LOOP_COUNT = 50;

    try {
      for (int i = 0; i < MAX_LOOP_COUNT; i++) {
        if (resultHandler.isReady) {
          break;
        }
        TimeUnit.MILLISECONDS.sleep(100);
      }
    } catch (InterruptedException exception) {
      throw new FailedToLoadSongException("Interrupted (internal).");
    }

    if (!resultHandler.isReady) {
      throw new FailedToLoadSongException("Timed out.");
    }
  }

  /**
   * Aggregates the results of multiple result handlers.
   * 
   * @param resultHandlers to aggregate.
   * @return aggregated result.
   */
  private static AggregatedResult getAggregatedResult(
      @NonNull List<TrackResolver.RapAudioLoadResultHandler> resultHandlers) {
    String failureMessage = null;
    final List<AudioTrack> resolvedTracks = new ArrayList<>();

    for (final TrackResolver.RapAudioLoadResultHandler resultHandler : resultHandlers) {
      final String resultHandlerFailureMessage = resultHandler
          .getFailureMessage();

      if (resultHandlerFailureMessage != null) {
        failureMessage = resultHandlerFailureMessage;
        break;
      }

      final AudioTrack[] resultHandlerResolvedTracks = resultHandler
          .getResolvedTracks();

      for (final AudioTrack resolvedTrack : resultHandlerResolvedTracks) {
        resolvedTracks.add(resolvedTrack);
      }
    }

    return new AggregatedResult(failureMessage,
        resolvedTracks.toArray(new AudioTrack[0]));
  }

  /**
   * Data class for the aggregated result of multiple result handlers.
   */
  public static class AggregatedResult {
    public final @Nullable String failureMessage;
    public final @NonNull AudioTrack[] audioTracks;

    public AggregatedResult(@Nullable String failureMessage,
        @NonNull AudioTrack[] audioTracks) {
      this.failureMessage = failureMessage;
      this.audioTracks = audioTracks;
    }
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
      int initialCap = this.asPlaylist ? Config.PLAYLIST_MAX_SIZE : 1;
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
