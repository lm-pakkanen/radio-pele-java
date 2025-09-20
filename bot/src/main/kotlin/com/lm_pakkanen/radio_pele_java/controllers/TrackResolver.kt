package com.lm_pakkanen.radio_pele_java.controllers

import com.lm_pakkanen.radio_pele_java.Config
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException
import com.lm_pakkanen.radio_pele_java.util.LavaLinkUtil.Companion.getLink
import dev.arbjerg.lavalink.client.player.LavalinkLoadResult
import dev.arbjerg.lavalink.client.player.Track
import org.springframework.util.Assert
import java.net.URI
import java.net.URISyntaxException

class TrackResolver(
  private val tidalController: TidalController,
  private val spotifyController: SpotifyController
) {

  /**
   * Try to resolve a song from a given URL. If the track can't be found, throws
   * an exception. Uses the LavaPlayer library to resolve the track. Spotify API
   * is used to resolve Spotify URLs into track and artist names.
   *
   * @param guildId    to use for connecting later on.
   * @param url        to try to resolve.
   * @param asPlaylist whether to resolve as a playlist or a single track.
   * @return resolved tracks.
   * @throws FailedToLoadSongException when song(s) cannot be resolved
   */
  @Throws(FailedToLoadSongException::class)
  fun resolve(guildId: Long, url: String, asPlaylist: Boolean): List<Track> {

    var asPlaylist = asPlaylist
    val capacity = if (asPlaylist) Config.PLAYLIST_MAX_SIZE else 1
    val finalUrls: MutableList<String> = ArrayList(capacity)

    var uri: URI

    try {
      uri = URI(url)
      Assert.notNull(uri.host, "URI host is null")
    } catch (ex: Exception) {
      when (ex) {

        is URISyntaxException, is IllegalArgumentException -> {
          throw FailedToLoadSongException("Invalid URL.")
        }

        else -> throw ex
      }

    }

    val uriDomain = uri.host

    if (uriDomain.contains("spotify")) {
      val qualifiedTrackNames = this.spotifyController.resolveQualifiedTrackNames(url)

      qualifiedTrackNames.stream().map { n -> "ytsearch:$n" }.forEach { n -> finalUrls.add(n) }
      asPlaylist = false
    } else if (uriDomain.contains("tidal")) {
      val qualifiedTrackNames = this.tidalController.resolveQualifiedTrackNames(url)

      for (qualifiedTrackName in qualifiedTrackNames) {
        finalUrls.add("ytsearch:$qualifiedTrackName")
      }

      asPlaylist = false
    } else {
      finalUrls.add(url)
    }

    if (finalUrls.isEmpty()) {
      throw FailedToLoadSongException("Not found.")
    }

    val audioLoader = AudioLoader(asPlaylist)

    finalUrls.forEach({ finalUrl: String ->
      val loadResult: LavalinkLoadResult =
        getLink(guildId).loadItem(finalUrl).block()
          ?: throw IllegalArgumentException("Could not load result for '$finalUrl'")

      audioLoader.accept(loadResult)
    })

    val resolvedTracks: MutableList<Track> = audioLoader.resolvedTracks

    if (resolvedTracks.isEmpty()) {
      throw FailedToLoadSongException("No tracks were resolved.")
    }

    return resolvedTracks
  }
}
