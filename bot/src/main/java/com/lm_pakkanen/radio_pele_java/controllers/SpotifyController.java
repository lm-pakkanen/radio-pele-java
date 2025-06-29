package com.lm_pakkanen.radio_pele_java.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
@Lazy
public final class SpotifyController {
  private final Config config;
  private final SpotifyApi spotifyApi;

  /**
   * Whether the Spotify API is authorised or not.
   */
  private boolean isUsable = true;

  /**
   * @param config instance.
   * @throws NullPointerException
   */
  public SpotifyController(@Autowired Config config)
      throws NullPointerException {

    this.config = config;

    final SpotifyApi.Builder spotifyApiBuilder = new SpotifyApi.Builder();

    spotifyApiBuilder.setClientId(this.config.spotifyClientId);
    spotifyApiBuilder.setClientSecret(this.config.spotifyClientSecret);

    this.spotifyApi = spotifyApiBuilder.build();
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
  public List<String> resolveQualifiedTrackNames(@Nullable String url)
      throws FailedToLoadSongException {

    if (url == null || url.isEmpty()) {
      throw new FailedToLoadSongException("URL is empty");
    }

    final boolean isAlbum = url.contains("/album/");
    final boolean isPlaylist = url.contains("/playlist/");

    final List<TrackSimplified> resolvedSimplifiedTracks = new ArrayList<>();

    try {
      final String entityId = getEntityIdFromUrl(url);

      if (isAlbum) {
        final Paging<TrackSimplified> albumSimplifiedTracks = this.spotifyApi
            .getAlbumsTracks(entityId).limit(Config.PLAYLIST_MAX_SIZE).build()
            .execute();

        resolvedSimplifiedTracks
            .addAll(List.of(albumSimplifiedTracks.getItems()));

      } else if (isPlaylist) {
        final PlaylistTrack[] playlistTrackItems = this.spotifyApi
            .getPlaylistsItems(entityId).limit(Config.PLAYLIST_MAX_SIZE).build()
            .execute().getItems();

        for (int i = 0; i < playlistTrackItems.length; i++) {
          final String playlistTrackItemString = JSON.std
              .asString(playlistTrackItems[i].getTrack());

          resolvedSimplifiedTracks.add(new TrackSimplified.JsonUtil()
              .createModelObject(playlistTrackItemString));

        }

      } else {
        final Track track = this.spotifyApi.getTrack(entityId).build()
            .execute();

        final String trackAsString = JSON.std.asString(track);

        resolvedSimplifiedTracks.add(
            new TrackSimplified.JsonUtil().createModelObject(trackAsString));
      }
    } catch (IOException | SpotifyWebApiException | ParseException exception) {
      String exceptionMessage = exception.getMessage();

      if (exceptionMessage == null) {
        exceptionMessage = "Unknown exception occurred.";
      }

      throw new FailedToLoadSongException(exceptionMessage);
    }

    final List<String> qualifiedTrackNames = new ArrayList<>();

    for (TrackSimplified resolvedTrack : resolvedSimplifiedTracks) {

      if (resolvedTrack == null) {
        break;
      }

      final List<ArtistSimplified> resolvedTrackArtists = List
          .of(resolvedTrack.getArtists());

      final ArtistSimplified artist = resolvedTrackArtists.stream()
          .filter(n -> n.getType().equals(ModelObjectType.ARTIST)).findFirst()
          .orElse(resolvedTrackArtists.getFirst());

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

      qualifiedTrackNames.add(qualifiedTrackName);

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
  private String getEntityIdFromUrl(String url)
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
