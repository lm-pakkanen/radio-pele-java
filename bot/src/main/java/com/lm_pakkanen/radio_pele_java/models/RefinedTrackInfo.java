package com.lm_pakkanen.radio_pele_java.models;

import dev.arbjerg.lavalink.client.player.Track;
import lombok.Getter;

@Getter
public final class RefinedTrackInfo {

  private final String title;

  private final String artist;

  private final String qualifiedName;

  private final String duration;

  public RefinedTrackInfo(Track track) {

    final long trackDurationMs = track.getInfo().getLength();

    final String trackTitle = track.getInfo().getTitle();
    final String trackArtist = track.getInfo().getAuthor();

    final StringBuilder qualifiedNameBuilder = new StringBuilder();
    qualifiedNameBuilder.append(trackArtist);
    qualifiedNameBuilder.append(" - ");
    qualifiedNameBuilder.append(trackTitle);

    this.title = trackTitle;
    this.artist = trackArtist;
    this.qualifiedName = qualifiedNameBuilder.toString();
    this.duration = formatDuration(trackDurationMs);
  }

  /**
   * Formats duration in milliseconds to a string in the format of
   * "<mm>min<ss>s" (e.g. 1min30s).
   * 
   * @param durationMs duration in milliseconds.
   * @return formatted duration string.
   */
  public static String formatDuration(long durationMs) {
    final long totalDurationSeconds = durationMs / 1000;

    final long durationMinutes = Math.floorDiv(totalDurationSeconds, 60);
    final long durationSeconds = Math.floorMod(totalDurationSeconds, 60);

    final StringBuilder formattedDurationBuilder = new StringBuilder();

    if (durationMinutes > 0) {
      formattedDurationBuilder.append(String.format("%dmin", durationMinutes));
    }

    if (durationSeconds > 0) {
      formattedDurationBuilder.append(String.format("%ds", durationSeconds));
    }

    String formattedDuration = formattedDurationBuilder.toString();

    if (formattedDuration.isEmpty()) {
      formattedDuration = "<n/a>";
    }

    return formattedDuration;
  }
}
