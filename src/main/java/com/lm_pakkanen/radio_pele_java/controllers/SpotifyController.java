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

import com.fasterxml.jackson.jr.ob.JSON;
import com.lm_pakkanen.radio_pele_java.Config;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

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

    final SpotifyApi spotifyApi = spotifyApiBuilder.build();

    if (spotifyApi == null) {
      throw new NullPointerException("Spotify API is null");
    }

    this.spotifyApi = spotifyApi;
    this.refreshAccessToken();
  }

  /**
   * Resolves a url to a qualified track name (<artist> - <track name>)
   * 
   * @param url to resolve.
   * @return qaualified track name (<artist> - <track name>).
   * @throws NullPointerException
   * @throws FailedToLoadSongException
   */
  public @NonNull String[] resolveQualifiedTrackNames(@Nullable String url)
      throws FailedToLoadSongException {

    if (url == null || url.isEmpty()) {
      throw new FailedToLoadSongException("URL is empty");
    }

    final boolean isAlbum = url.contains("/album/");
    final boolean isPlaylist = url.contains("/playlist/");

    final int capacity = isAlbum || isPlaylist ? 15 : 1;

    TrackSimplified[] resolvedSimplifiedTracks = new TrackSimplified[capacity];

    try {
      final String entityId = getEntityIdFromUrl(url);

      if (isAlbum) {
        final Paging<TrackSimplified> albumSimplifiedTracks = this.spotifyApi
            .getAlbumsTracks(entityId).limit(15).build().execute();

        resolvedSimplifiedTracks = albumSimplifiedTracks.getItems();

      } else if (isPlaylist) {
        final PlaylistTrack[] playlistTrackItems = this.spotifyApi
            .getPlaylistsItems(entityId).limit(15).build().execute().getItems();

        for (int i = 0; i < playlistTrackItems.length; i++) {
          final String playlistTrackItemString = JSON.std
              .asString(playlistTrackItems[i].getTrack());

          resolvedSimplifiedTracks[i] = new TrackSimplified.JsonUtil()
              .createModelObject(playlistTrackItemString);
        }

      } else {
        final Track track = this.spotifyApi.getTrack(entityId).build()
            .execute();

        final String trackAsString = JSON.std.asString(track);

        resolvedSimplifiedTracks[0] = new TrackSimplified.JsonUtil()
            .createModelObject(trackAsString);
      }
    } catch (IOException | SpotifyWebApiException | ParseException exception) {
      String exceptionMessage = exception.getMessage();

      if (exceptionMessage == null) {
        exceptionMessage = "Unknown exception occurred.";
      }

      throw new FailedToLoadSongException(exceptionMessage);
    }

    final String[] qualifiedTrackNames = new String[capacity];

    for (int i = 0; i < resolvedSimplifiedTracks.length; i++) {
      final TrackSimplified resolvedTrack = resolvedSimplifiedTracks[i];

      final ArtistSimplified[] resolvedTrackArtists = resolvedTrack
          .getArtists();

      final ArtistSimplified artist = List.of(resolvedTrackArtists).stream()
          .filter(n -> n.getType().equals(ModelObjectType.ARTIST)).findFirst()
          .orElse(resolvedTrackArtists[0]);

      final StringBuilder qualifiedTrackNameBuilder = new StringBuilder();

      if (artist != null) {
        qualifiedTrackNameBuilder.append(artist.getName());
        qualifiedTrackNameBuilder.append(" - ");
      }

      qualifiedTrackNameBuilder.append(resolvedTrack.getName());

      final String qualifiedTrackName = qualifiedTrackNameBuilder.toString();

      if (qualifiedTrackName == null) {
        continue;
      }

      qualifiedTrackNames[i] = qualifiedTrackName;

    }

    if (qualifiedTrackNames.length == 0) {
      throw new FailedToLoadSongException("Not found.");
    }

    return qualifiedTrackNames;
  }

  /**
   * @return boolean whether the Spotify API is usable or not.
   */
  public boolean getIsUsable() {
    return this.isUsable;
  }

  /**
   * Gets entity ID from the given URL.
   * 
   * @param url to get entity ID from.
   * @return entity ID.
   * @throws FailedToLoadSongException
   */
  private @NonNull String getEntityIdFromUrl(@NonNull String url)
      throws FailedToLoadSongException {

    final int entityIdStartIndex = url.lastIndexOf("/") + 1;

    int entityIdEndIndex = url.length();

    if (url.contains("?")) {
      entityIdEndIndex = url.indexOf("?");
    }

    if (entityIdEndIndex <= entityIdStartIndex) {
      throw new FailedToLoadSongException("Failed to get entity ID from URL");
    }

    final String entityId = url.substring(entityIdStartIndex, entityIdEndIndex);

    if (entityId == null || entityId.isEmpty()) {
      throw new FailedToLoadSongException("Failed to get entity ID from URL");
    }

    return entityId;
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
