package com.lm_pakkanen.radio_pele_java.controllers;

import java.util.ArrayList;
import java.util.List;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.SearchResult;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackLoaded;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class AudioLoader extends AbstractAudioLoadResultHandler {

  @Getter
  private final List<Track> resolvedTracks = new ArrayList<>();

  private final boolean asPlaylist;

  @Override
  public void ontrackLoaded(TrackLoaded result) {
    final Track track = result.getTrack();
    log.info("Loaded track '{}'", track.getInfo().getTitle());
    resolvedTracks.add(track);
  }

  @Override
  public void onPlaylistLoaded(PlaylistLoaded result) {

    final List<Track> loadedTracks = result.getTracks();
    log.info("Playlist loaded with {} tracks", loadedTracks.size());

    if (asPlaylist) {
      log.info("Resolving {} tracks", loadedTracks.size());
      resolvedTracks.addAll(loadedTracks);
    } else {
      log.info("Resolving first track from playlist");
      resolvedTracks.add(loadedTracks.get(0));
    }
  }

  @Override
  public void onSearchResultLoaded(SearchResult result) {

    final List<Track> tracks = result.getTracks();

    if (tracks.isEmpty()) {
      log.error("No tracks found in search result");
      return;
    }

    final Track firstTrack = tracks.get(0);
    resolvedTracks.add(firstTrack);
  }

  @Override
  public void noMatches() {
    log.error("No matches found for the provided search criteria.");
  }

  @Override
  public void loadFailed(LoadFailed result) {
    log.error("Failed to load audio: {}", result.getException().getMessage());
  }
}
