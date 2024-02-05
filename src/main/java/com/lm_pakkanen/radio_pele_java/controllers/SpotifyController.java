package com.lm_pakkanen.radio_pele_java.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.Config;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

@Component
public final class SpotifyController {
  private final @NonNull Config config;
  private final @NonNull SpotifyApi spotifyApi;

  // Whether the Spotify API is usable or not.
  private boolean isUsable = true;

  /**
   * @param config instance.
   * @throws NullPointerException
   */
  public SpotifyController(@Autowired @NonNull Config config)
      throws NullPointerException {
    this.config = config;

    final SpotifyApi.Builder spotifyApiBuilder = new SpotifyApi.Builder();

    spotifyApiBuilder.setClientId(this.config.SPOTIFY_CLIENT_ID);
    spotifyApiBuilder.setClientSecret(this.config.SPOTIFY_CLIENT_SECRET);

    SpotifyApi spotifyApi = spotifyApiBuilder.build();

    if (spotifyApi == null) {
      throw new NullPointerException("Spotify API is null");
    }

    this.spotifyApi = spotifyApi;
    this.refreshAccessToken();
  }

  /**
   * Resolves a url to a qualified track name (<artist< - <track name>)
   * 
   * @param url to resolve.
   * @return qaualified track name (<artist< - <track name>).
   * @throws NullPointerException
   * @throws FailedToLoadSongException
   */
  public @NonNull String resolveQualifiedTrackName(@Nullable String url)
      throws NullPointerException, FailedToLoadSongException {

    if (url == null || url.isEmpty()) {
      throw new FailedToLoadSongException("URL is empty");
    }

    final String trackId = getTrackIdFromUrl(url);

    try {
      final Track resolvedTrack = this.spotifyApi.getTrack(trackId).build()
          .execute();

      final ArtistSimplified[] resolvedTrackArtists = resolvedTrack
          .getArtists();

      final ArtistSimplified artist = List.of(resolvedTrackArtists).stream()
          .filter(n -> n.getType().equals(ModelObjectType.ARTIST)).findFirst()
          .orElse(null);

      final StringBuilder qualifiedTrackNameBuilder = new StringBuilder();

      if (artist != null) {
        qualifiedTrackNameBuilder.append(artist.getName());
        qualifiedTrackNameBuilder.append(" - ");
      }

      qualifiedTrackNameBuilder.append(resolvedTrack.getName());

      final String qualifiedTrackName = qualifiedTrackNameBuilder.toString();

      if (qualifiedTrackName == null) {
        throw new NullPointerException("Qualified track name is null");
      }

      return qualifiedTrackName;
    } catch (IOException | SpotifyWebApiException | ParseException exception) {
      String exceptionMessage = exception.getMessage();

      if (exceptionMessage == null) {
        exceptionMessage = "Unknown exception occurred.";
      }

      throw new FailedToLoadSongException(exceptionMessage);
    }
  }

  /**
   * @return boolean whether the Spotify API is usable or not.
   */
  public boolean getIsUsable() {
    return this.isUsable;
  }

  /**
   * Gets track ID from the given URL.
   * 
   * @param url to get track ID from.
   * @return track ID.
   * @throws FailedToLoadSongException
   */
  private @NonNull String getTrackIdFromUrl(@NonNull String url)
      throws FailedToLoadSongException {

    final int trackIdStartIndex = url.lastIndexOf("/") + 1;

    int trackIdEndIndex = url.length() - 1;

    if (url.contains("?")) {
      trackIdEndIndex = url.indexOf("?");
    }

    if (trackIdEndIndex <= trackIdStartIndex) {
      throw new FailedToLoadSongException("Failed to get track ID from URL");
    }

    final String trackId = url.substring(trackIdStartIndex, trackIdEndIndex);

    if (trackId == null || trackId.isEmpty()) {
      throw new FailedToLoadSongException("Failed to get track ID from URL");
    }

    return trackId;
  }

  /**
   * Refreshes the access token and schedules the next refresh recursively.
   */
  private void refreshAccessToken() {
    try {
      final ClientCredentials clientCredentials = this.spotifyApi
          .clientCredentials().build().execute();

      final String accessToken = clientCredentials.getAccessToken();
      this.spotifyApi.setAccessToken(accessToken);
      this.isUsable = true;

      final int nextRefreshExpiresInSeconds = clientCredentials.getExpiresIn();

      /**
       * Time next access token refresh to happen 5 minutes before the current
       * token expires
       */
      final int nextRefreshDelayInSeconds = nextRefreshExpiresInSeconds
          - 5 * 60;

      final int nextRefreshDelayInMilliseconds = nextRefreshDelayInSeconds
          * 1000;

      final Timer refreshAccessTokenTimer = new Timer();

      final TimerTask refreshAccessTokenTimerTask = new TimerTask() {
        @Override
        public void run() {
          refreshAccessToken();
          refreshAccessTokenTimer.cancel();
        }
      };

      // Schedule this method again.
      refreshAccessTokenTimer.schedule(refreshAccessTokenTimerTask,
          nextRefreshDelayInMilliseconds);

    } catch (IOException | SpotifyWebApiException | ParseException exception) {
      exception.printStackTrace();
      this.isUsable = false;
    }
  }
}
