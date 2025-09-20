package com.lm_pakkanen.radio_pele_java.controllers

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.client.player.TrackLoaded
import lombok.extern.log4j.Log4j2

@Log4j2
class AudioLoader(
    private val asPlaylist: Boolean,
) : AbstractAudioLoadResultHandler() {
    val resolvedTracks: MutableList<Track> = ArrayList()

    override fun ontrackLoaded(result: TrackLoaded) {
        val track = result.track
        resolvedTracks.add(track)
    }

    override fun onPlaylistLoaded(result: PlaylistLoaded) {
        val loadedTracks: List<Track> = result.tracks

        if (asPlaylist) {
            resolvedTracks.addAll(loadedTracks)
        } else {
            resolvedTracks.add(loadedTracks[0])
        }
    }

    override fun onSearchResultLoaded(result: SearchResult) {
        val tracks: List<Track> = result.tracks

        if (tracks.isEmpty()) {
            return
        }

        val firstTrack = tracks[0]
        resolvedTracks.add(firstTrack)
    }

    override fun noMatches(): Unit = throw IllegalArgumentException("No matches found")

    override fun loadFailed(result: LoadFailed): Unit = throw IllegalArgumentException("Failed to load track: ${result.exception.message}")
}
