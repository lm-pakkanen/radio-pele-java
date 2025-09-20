package com.lm_pakkanen.radio_pele_java.models

import dev.arbjerg.lavalink.client.player.Track

class RefinedTrackInfo(
    track: Track,
) {
    val title: String
    val artist: String
    val qualifiedName: String
    val duration: String

    init {
        val trackDurationMs = track.info.length

        val trackTitle = track.info.title
        val trackArtist = track.info.author

        val qualifiedNameBuilder = StringBuilder()
        qualifiedNameBuilder.append(trackArtist)
        qualifiedNameBuilder.append(" - ")
        qualifiedNameBuilder.append(trackTitle)

        this.title = trackTitle
        this.artist = trackArtist
        this.qualifiedName = qualifiedNameBuilder.toString()
        this.duration = formatDuration(trackDurationMs)
    }

    companion object {
        /**
         * Formats duration in milliseconds to a string in the format of
         * "<mm>min<ss>s" (e.g. 1min30s).
         *
         * @param durationMs duration in milliseconds.
         * @return formatted duration string.
         */
        @JvmStatic
        fun formatDuration(durationMs: Long): String {
            val totalDurationSeconds = durationMs / 1000

            val durationMinutes = Math.floorDiv(totalDurationSeconds, 60)
            val durationSeconds = Math.floorMod(totalDurationSeconds, 60).toLong()

            val formattedDurationBuilder = StringBuilder()

            if (durationMinutes > 0) {
                formattedDurationBuilder.append(String.format("%dmin", durationMinutes))
            }

            if (durationSeconds > 0) {
                formattedDurationBuilder.append(String.format("%ds", durationSeconds))
            }

            var formattedDuration = formattedDurationBuilder.toString()

      if (formattedDuration.isEmpty()) {
        formattedDuration = "<n/a>"
      }

      return formattedDuration
    }
  }
}
