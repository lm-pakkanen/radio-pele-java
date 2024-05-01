package com.lm_pakkanen.radio_pele_java.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.Config;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;

import io.github.lm_pakkanen.tidal_api.TidalApi;
import io.github.lm_pakkanen.tidal_api.models.CredentialsStore;
import io.github.lm_pakkanen.tidal_api.models.entities.TidalCredentials;
import io.github.lm_pakkanen.tidal_api.models.entities.TidalSimpleArtist;
import io.github.lm_pakkanen.tidal_api.models.entities.TidalTrack;
import io.github.lm_pakkanen.tidal_api.models.exceptions.InvalidCredentialsException;
import io.github.lm_pakkanen.tidal_api.models.exceptions.QueryException;
import io.github.lm_pakkanen.tidal_api.models.exceptions.UnauthorizedException;

@Component
@Lazy
public class TidalController {
  private final @NonNull Config config;
  private final @NonNull TidalApi tidalApi;

  /**
   * Whether the Tidal API is authorised or not.
   */
  private boolean isUsable = true;

  /**
   * @param config instance.
   * @throws NullPointerException if the Tidal API instance cannot be created.
   */
  public TidalController(@Autowired @NonNull Config config) {
    final TidalApi tidalApi = new TidalApi();

    this.config = config;
    this.tidalApi = tidalApi;

    refreshAccessToken();
  }

  public @NonNull String[] resolveQualifiedTrackNames(@Nullable String url)
      throws FailedToLoadSongException {

    if (url == null || url.isEmpty()) {
      throw new FailedToLoadSongException("URL is empty");
    }

    final boolean isArtist = url.contains("/artist/");
    final boolean isAlbum = url.contains("/album/");
    final boolean isMix = url.contains("/mix/");
    final boolean isPlaylist = url.contains("/playlist/");
    final boolean isVideo = url.contains("/video/");

    final boolean resolveAsPlaylist = isArtist || isAlbum || isPlaylist;

    final int capacity = resolveAsPlaylist ? 15 : 1;

    final String entityId = getEntityIdFromUrl(url);

    final TidalTrack[] resolvedTracks = getResolvedTracks(entityId, isArtist,
        isAlbum, isMix, isPlaylist, isVideo);

    final String[] qualifiedTrackNames = new String[capacity];

    for (int i = 0; i < resolvedTracks.length; i++) {
      final TidalTrack resolvedTrack = resolvedTracks[i];

      if (resolvedTrack == null) {
        break;
      }

      final List<TidalSimpleArtist> resolvedTrackArtists = Arrays
          .asList(resolvedTrack.getArtists());

      final TidalSimpleArtist artist = resolvedTrackArtists.stream()
          .filter(n -> n.isMainArtist).findFirst()
          .orElse(resolvedTrackArtists.get(0));

      final StringBuilder qualifiedTrackNameBuilder = new StringBuilder();

      if (artist != null) {
        qualifiedTrackNameBuilder.append(artist.name);
        qualifiedTrackNameBuilder.append(" - ");
      }

      qualifiedTrackNameBuilder.append(resolvedTrack.getTitle());

      final String qualifiedTrackName = qualifiedTrackNameBuilder.toString();

      if (qualifiedTrackName == null) {
        continue;
      }

      qualifiedTrackNames[i] = qualifiedTrackName;
    }

    return qualifiedTrackNames;
  }

  private @NonNull TidalTrack[] getResolvedTracks(@NonNull String entityId,
      boolean isArtist, boolean isAlbum, boolean isMix, boolean isPlaylist,
      boolean isVideo) throws FailedToLoadSongException {

    if (isAlbum) {
      throw new FailedToLoadSongException("Albums not supported yet");
    }

    if (isMix) {
      throw new FailedToLoadSongException("Mixes not supported yet");
    }

    if (isPlaylist) {
      throw new FailedToLoadSongException("Playlists not supported yet");
    }

    if (isVideo) {
      throw new FailedToLoadSongException("Videos not supported yet");
    }

    try {

      if (isArtist) {
        return this.tidalApi.tracks.listByArtist(entityId, "FI", 15);
      }

      final TidalTrack[] tracks = new TidalTrack[] {
          this.tidalApi.tracks.get(entityId, "FI")
      };

      return tracks;
    } catch (QueryException exception) {
      throw new FailedToLoadSongException(exception.getMessage());
    }
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
   * @return boolean whether the Tidal API is usable or not.
   */
  public boolean getIsUsable() {
    return this.isUsable;
  }

  /**
   * Refreshes the access token and schedules the next refresh recursively.
   */
  private void refreshAccessToken() {
    final String tidalClientId = this.config.TIDAL_CLIENT_ID;
    final String tidalClientSecret = this.config.TIDAL_CLIENT_SECRET;

    try {
      this.tidalApi.authorize(tidalClientId, tidalClientSecret);
      this.isUsable = true;
    } catch (InvalidCredentialsException | UnauthorizedException exception) {
      this.isUsable = false;
      return;
    }

    final TidalCredentials credentials = CredentialsStore.getInstance()
        .getCredentials();

    final long nextRefreshExpiresInSeconds = credentials.getExpiresInSeconds();

    /**
     * Time next access token refresh to happen 5 minutes before the current
     * token expires
     */
    final long nextRefreshDelayInSeconds = nextRefreshExpiresInSeconds - 5 * 60;

    final long nextRefreshDelayInMs = nextRefreshDelayInSeconds * 1000;
    final Timer refreshAccessTokenTimer = new Timer();

    final TimerTask refreshTokenTask = new TimerTask() {
      @Override
      public void run() {
        refreshAccessToken();
      }
    };

    // Schedule this method again.
    refreshAccessTokenTimer.schedule(refreshTokenTask, nextRefreshDelayInMs);
  }
}
