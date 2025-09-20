package com.lmpakkanen.radiopelejava.controllers

import com.lmpakkanen.radiopelejava.Config
import com.lmpakkanen.radiopelejava.models.exceptions.FailedToLoadSongException
import io.github.lm_pakkanen.tidal_api.TidalApi
import io.github.lm_pakkanen.tidal_api.models.CredentialsStore
import io.github.lm_pakkanen.tidal_api.models.entities.TidalTrack
import io.github.lm_pakkanen.tidal_api.models.exceptions.InvalidCredentialsException
import io.github.lm_pakkanen.tidal_api.models.exceptions.QueryException
import io.github.lm_pakkanen.tidal_api.models.exceptions.UnauthorizedException
import org.springframework.stereotype.Component
import java.util.Timer
import java.util.TimerTask

@Component
class TidalController(
    private val config: Config,
) {
    private val tidalApi: TidalApi = TidalApi()

    /**
     * Whether the Tidal API is authorised or not.
     */
    var isUsable: Boolean = true
        private set

    /**
     * @param config instance.
     */
    init {
        refreshAccessToken()
    }

    @Throws(FailedToLoadSongException::class)
    fun resolveQualifiedTrackNames(url: String?): Array<String> {
        if (url.isNullOrEmpty()) {
            throw FailedToLoadSongException("URL is empty")
        }

        val isArtist = url.contains("/artist/")
        val isAlbum = url.contains("/album/")
        val isMix = url.contains("/mix/")
        val isPlaylist = url.contains("/playlist/")
        val isVideo = url.contains("/video/")

        val resolveAsPlaylist = isArtist || isAlbum || isPlaylist

        val capacity = if (resolveAsPlaylist) Config.PLAYLIST_MAX_SIZE else 1
        val entityId = getEntityIdFromUrl(url)

        val resolvedTracks =
            getResolvedTracks(
                entityId,
                isArtist,
                isAlbum,
                isMix,
                isPlaylist,
                isVideo,
            )

        val qualifiedTrackNames: MutableList<String> = ArrayList(capacity)

        for (i in resolvedTracks.indices) {
            val resolvedTrack = resolvedTracks[i] ?: break
            val resolvedTrackArtists = listOf(*resolvedTrack.artists)

            val artist =
                resolvedTrackArtists
                    .stream()
                    .filter { n -> n.isMainArtist }
                    .findFirst()
                    .orElse(resolvedTrackArtists[0])

            val qualifiedTrackNameBuilder = StringBuilder()

            if (artist != null) {
                qualifiedTrackNameBuilder.append(artist.name)
                qualifiedTrackNameBuilder.append(" - ")
            }

            qualifiedTrackNameBuilder.append(resolvedTrack.title)

            val qualifiedTrackName = qualifiedTrackNameBuilder.toString()
            qualifiedTrackNames[i] = qualifiedTrackName
        }

        return qualifiedTrackNames.toTypedArray()
    }

    @Throws(FailedToLoadSongException::class)
    private fun getResolvedTracks(
        entityId: String,
        isArtist: Boolean,
        isAlbum: Boolean,
        isMix: Boolean,
        isPlaylist: Boolean,
        isVideo: Boolean,
    ): Array<TidalTrack?> {
        if (isAlbum) {
            throw UnsupportedOperationException("Albums not supported yet")
        }

        if (isMix) {
            throw UnsupportedOperationException("Mixes not supported yet")
        }

        if (isPlaylist) {
            throw UnsupportedOperationException("Playlists not supported yet")
        }

        if (isVideo) {
            throw UnsupportedOperationException("Videos not supported yet")
        }

        try {
            if (isArtist) {
                return this.tidalApi.tracks.listByArtist(
                    entityId,
                    "FI",
                    Config.PLAYLIST_MAX_SIZE,
                )
            }

            return arrayOf(
                this.tidalApi.tracks[entityId, "FI"],
            )
        } catch (exception: QueryException) {
            throw FailedToLoadSongException(exception.message!!)
        }
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
        val tidalClientId = this.config.tidalClientId
        val tidalClientSecret = this.config.tidalClientSecret

        try {
            this.tidalApi.authorize(tidalClientId, tidalClientSecret)
            this.isUsable = true
        } catch (_: InvalidCredentialsException) {
            this.isUsable = false
            return
        } catch (_: UnauthorizedException) {
            this.isUsable = false
            return
        }

        val credentials = CredentialsStore.getInstance().credentials
        val nextRefreshExpiresInSeconds = credentials.getExpiresInSeconds()

        /**
         * Time next access token refresh to happen 5 minutes before the current
         * token expires
         */
        val nextRefreshDelayInSeconds: Long = nextRefreshExpiresInSeconds - 5 * 60

        val nextRefreshDelayInMs: Long = nextRefreshDelayInSeconds * 1000
        val refreshAccessTokenTimer = Timer()

        val refreshTokenTask: TimerTask =
            object : TimerTask() {
                override fun run() {
                    refreshAccessToken()
                }
            }

        // Schedule this method again.
        refreshAccessTokenTimer.schedule(refreshTokenTask, nextRefreshDelayInMs)
    }
}
