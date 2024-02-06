package com.lm_pakkanen.radio_pele_java.models;

import org.springframework.lang.NonNull;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class TrackInfo {
  private final @NonNull String title;
  private final @NonNull String artist;
  private final @NonNull String qualifiedName;
  private final @NonNull String duration;

  /**
   * @param track instance.
   */
  public TrackInfo(@NonNull AudioTrack track) {
    final long trackDurationMs = track.getDuration();

    String trackTitle = track.getInfo().title;
    String trackArtist = track.getInfo().author;

    if (trackTitle == null) {
      trackTitle = "Unknown track";
    }

    if (trackArtist == null) {
      trackArtist = "Unknown artist";
    }

    StringBuilder qualifiedNameBuilder = new StringBuilder();
    qualifiedNameBuilder.append(trackArtist);
    qualifiedNameBuilder.append(" - ");
    qualifiedNameBuilder.append(trackTitle);

    String qualifiedName = qualifiedNameBuilder.toString();

    if (qualifiedName == null) {
      throw new NullPointerException("Qualified name is null");
    }

    this.title = trackTitle;
    this.artist = trackArtist;
    this.qualifiedName = qualifiedName;
    this.duration = TrackInfo.formatDuration(trackDurationMs);
  }

  /**
   * @return track title.
   */
  public @NonNull String getTitle() {
    return this.title;
  }

  /**
   * @return track artist.
   */
  public @NonNull String getArtist() {
    return this.artist;
  }

  /**
   * @return track's qualified name.
   */
  public @NonNull String getQualifiedName() {
    return this.qualifiedName;
  }

  /**
   * @return track's duration.
   */
  public @NonNull String getDuration() {
    return this.duration;
  }

  /**
   * Formats duration in milliseconds to a string in the format of
   * "<mm>min<ss>s" (e.g. 1min30s).
   * 
   * @param durationMs duration in milliseconds.
   * @return formatted duration string.
   */
  private static @NonNull String formatDuration(long durationMs) {
    final long totalDurationSeconds = durationMs / 1000;

    final long durationMinutes = Math.floorDiv(totalDurationSeconds, 60);
    final long durationSeconds = Math.floorMod(totalDurationSeconds, 60);

    final String formattedDuration = String.format("%dmin%ds", durationMinutes,
        durationSeconds);

    if (formattedDuration == null) {
      throw new NullPointerException("Formatted duration is null");
    }

    return formattedDuration;
  }
}
