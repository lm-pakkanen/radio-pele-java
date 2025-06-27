package com.lm_pakkanen.radio_pele_java.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.Assert;
import com.lm_pakkanen.radio_pele_java.Config;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;
import com.lm_pakkanen.radio_pele_java.util.LavaLinkUtil;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkLoadResult;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TrackResolver {

  private final TidalController tidalController;
  private final SpotifyController spotifyController;

  /**
   * Try to resolve a song from a given URL. If the track can't be found, throws
   * an exception. Uses the LavaPlayer library to resolve the track. Spotify API
   * is used to resolve Spotify URLs into track and artist names.
   * 
   * @param link       to use for resolving.
   * @param url        to try to resolve.
   * @param asPlaylist whether to resolve as a playlist or a single track.
   * @return resolved tracks.
   * @throws FailedToLoadSongException
   */
  public List<Track> resolve(long guildId, String url, boolean asPlaylist)
      throws FailedToLoadSongException {

    final int capacity = asPlaylist ? Config.PLAYLIST_MAX_SIZE : 1;
    final List<String> finalUrls = new ArrayList<>(capacity);

    URI uri = null;

    try {
      uri = new URI(url);
      Assert.notNull(uri.getHost(), "URI host is null");
    } catch (URISyntaxException | IllegalArgumentException _) {
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

    if (finalUrls.isEmpty()) {
      throw new FailedToLoadSongException("Not found.");
    }

    final AudioLoader audioLoader = new AudioLoader(asPlaylist);

    finalUrls.stream().forEach(finalUrl -> {
      final LavalinkLoadResult loadResult = LavaLinkUtil.getLink(guildId)
          .loadItem(finalUrl).block();
      audioLoader.accept(loadResult);
    });

    final List<Track> resolvedTracks = audioLoader.getResolvedTracks();

    if (resolvedTracks.isEmpty()) {
      throw new FailedToLoadSongException("No tracks were resolved.");
    }

    return resolvedTracks;
  }
}
