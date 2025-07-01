package com.lm_pakkanen.radio_pele_java.controllers

import com.fasterxml.jackson.jr.ob.JSON
import com.lm_pakkanen.radio_pele_java.Config
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException
import org.apache.hc.core5.http.ParseException
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.enums.ModelObjectType
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified
import java.io.IOException
import java.util.Timer
import java.util.TimerTask

@Component
@Lazy
class SpotifyController(
  private val config: Config
) {

  private val spotifyApi: SpotifyApi

  /** Whether the Spotify API is usable or not. */
  var isUsable: Boolean = true
    private set

  /**
   * @param config instance.
   * @throws NullPointerException
   */
  init {
    val spotifyApiBuilder = SpotifyApi.Builder()
    spotifyApiBuilder.setClientId(this.config.spotifyClientId)
    spotifyApiBuilder.setClientSecret(this.config.spotifyClientSecret)
    this.spotifyApi = spotifyApiBuilder.build()
    this.refreshAccessToken()
  }

  /**
   * Resolves a url to a qualified track name (<artist> - <track name></track>)
   *
   * @param url to resolve.
   * @return qaualified track name (<artist> - <track name></track>).
   * @throws NullPointerException
   * @throws FailedToLoadSongException
  </artist></artist> */
  @Throws(FailedToLoadSongException::class)
  fun resolveQualifiedTrackNames(url: String?): MutableList<String> {

    if (url == null || url.isEmpty()) {
      throw FailedToLoadSongException("URL is empty")
    }

    val isAlbum = url.contains("/album/")
    val isPlaylist = url.contains("/playlist/")

    val resolvedSimplifiedTracks: MutableList<TrackSimplified> = ArrayList()

    try {
      val entityId = getEntityIdFromUrl(url)

      if (isAlbum) {
        val albumSimplifiedTracks = this.spotifyApi
          .getAlbumsTracks(entityId).limit(Config.PLAYLIST_MAX_SIZE).build()
          .execute()

        resolvedSimplifiedTracks.addAll(albumSimplifiedTracks.getItems())
      } else if (isPlaylist) {

        val playlistTrackItems = this.spotifyApi
          .getPlaylistsItems(entityId)
          .limit(Config.PLAYLIST_MAX_SIZE)
          .build()
          .execute()
          .getItems()

        for (i: Int in playlistTrackItems.indices) {
          val playlistTrackItemString = JSON.std
            .asString(playlistTrackItems[i].track)

          resolvedSimplifiedTracks.add(
            TrackSimplified.JsonUtil()
              .createModelObject(playlistTrackItemString)
          )
        }
      } else {
        val track = this.spotifyApi.getTrack(entityId).build()
          .execute()

        val trackAsString = JSON.std.asString(track)

        resolvedSimplifiedTracks.add(
          TrackSimplified.JsonUtil().createModelObject(trackAsString)
        )
      }
    } catch (exception: Exception) {
      when (exception) {
        is IOException, is SpotifyWebApiException, is ParseException -> {

          var exceptionMessage = exception.message

          if (exceptionMessage == null) {
            exceptionMessage = "Unknown exception occurred."
          }

          throw FailedToLoadSongException(exceptionMessage)
        }

        else -> throw exception
      }
    }

    val qualifiedTrackNames: MutableList<String> = ArrayList()

    for (resolvedTrack in resolvedSimplifiedTracks) {

      val resolvedTrackArtists: List<ArtistSimplified> = listOf(*resolvedTrack.artists)

      val artist = resolvedTrackArtists.stream()
        .filter { n -> n.type == ModelObjectType.ARTIST }
        .findFirst()
        .orElse(resolvedTrackArtists.first())

      val qualifiedTrackNameBuilder = StringBuilder()

      if (artist != null) {
        qualifiedTrackNameBuilder.append(artist.name)
        qualifiedTrackNameBuilder.append(" - ")
      }

      qualifiedTrackNameBuilder.append(resolvedTrack.name)
      qualifiedTrackNames.add(qualifiedTrackNameBuilder.toString())
    }

    return qualifiedTrackNames
  }

  /**
   * Gets entity ID from the given URL.
   *
   * @param url to get entity ID from.
   * @return entity ID.
   * @throws FailedToLoadSongException
   */
  @Throws(FailedToLoadSongException::class)
  private fun getEntityIdFromUrl(url: String): String {

    val entityIdStartIndex = url.lastIndexOf("/") + 1

    var entityIdEndIndex = url.length

    if (url.contains("?")) {
      entityIdEndIndex = url.indexOf("?")
    }

    if (entityIdEndIndex <= entityIdStartIndex) {
      throw FailedToLoadSongException("Failed to get entity ID from URL")
    }

    val entityId = url.substring(entityIdStartIndex, entityIdEndIndex)

    if (entityId.isEmpty()) {
      throw FailedToLoadSongException("Failed to get entity ID from URL")
    }

    return entityId
  }

  /**
   * Refreshes the access token and schedules the next refresh recursively.
   */
  private fun refreshAccessToken() {
    try {
      val clientCredentials = this.spotifyApi
        .clientCredentials().build().execute()

      val accessToken = clientCredentials.accessToken
      this.spotifyApi.accessToken = accessToken
      this.isUsable = true

      val nextRefreshExpiresInSeconds = clientCredentials.expiresIn

      /**
       * Time next access token refresh to happen 5 minutes before the current
       * token expires
       */
      val nextRefreshDelayInSeconds = (nextRefreshExpiresInSeconds
          - 5 * 60)

      val nextRefreshDelayInMilliseconds = (nextRefreshDelayInSeconds
          * 1000)

      val refreshAccessTokenTimer = Timer()

      val refreshAccessTokenTimerTask: TimerTask = object : TimerTask() {
        override fun run() {
          refreshAccessToken()
        }
      }

      // Schedule this method again.
      refreshAccessTokenTimer.schedule(
        refreshAccessTokenTimerTask,
        nextRefreshDelayInMilliseconds.toLong()
      )
    } catch (exception: Exception) {
      when (exception) {
        
        is IOException, is SpotifyWebApiException, is ParseException -> {
          exception.printStackTrace()
          this.isUsable = false
        }

        else -> throw exception
      }
    }
  }
}
