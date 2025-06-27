package com.lm_pakkanen.radio_pele_java.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.stereotype.Component;
import com.lm_pakkanen.radio_pele_java.Config;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public final class Store {

  private final List<Track> playListQueue = new ArrayList<>(1);
  private final BlockingQueue<Track> queue = new LinkedBlockingQueue<>();

  /**
   * Adds a track to the queue.
   * 
   * @param track to add.
   */
  public boolean add(Optional<Track> track) {

    if (track.isEmpty()) {
      return false;
    }

    return this.queue.offer(track.get());
  }

  /**
   * Adds a list of tracks to the queue.
   * 
   * @param urls to add.
   */
  public void addPlaylist(List<Track> audioTracks) {

    final int playlistSize = Math.min(Config.PLAYLIST_MAX_SIZE,
        audioTracks.size());

    this.playListQueue.clear();
    this.playListQueue.addAll(audioTracks.subList(0, playlistSize));
  }

  /**
   * Gets and removes the first track from the queue.
   * 
   * @return the first track from the queue or null if queue is empty.
   */
  public Optional<Track> shift() {

    log.info("Shifting queue");
    Optional<Track> trackOpt = Optional.ofNullable(queue.poll());

    if (trackOpt.isPresent()) {

      final TrackInfo trackInfo = trackOpt.get().getInfo();
      final String title = trackInfo == null ? "Unknown title"
          : trackInfo.getTitle();

      log.debug("Shited queue, found track: '{}'", title);
    } else {
      log.debug("Track not found");
    }

    return trackOpt;
  }

  /**
   * Gets and removes the first track from the playlist queue.
   * 
   * @return the first track from the queue or null if queue is empty.
   */
  public Optional<Track> shiftPlaylist() {

    if (this.playListQueue.isEmpty()) {
      return Optional.empty();
    }

    return Optional.ofNullable(this.playListQueue.remove(0));
  }

  public void clear() {
    this.queue.clear();
  }

  public void clearPlaylist() {
    this.playListQueue.clear();
  }

  /**
   * Shuffles the queue.
   */
  public void shuffle() {
    log.info("Shuffling queue");
    final List<Track> shuffledTracks = new ArrayList<>(this.queue);
    Collections.shuffle(shuffledTracks);
    this.queue.clear();
    this.queue.addAll(shuffledTracks);
    log.debug("Queue shuffled");
  }

  /**
   * Shuffles the playlist queue.
   */
  public void shufflePlaylist() {

    log.debug("Shuffling playlist queue");
    if (!this.hasPlaylist()) {
      log.debug("Could not shuffle playlist queue: not a playlist");
      return;
    }

    final List<Track> shuffledUrls = new ArrayList<>(this.playListQueue);
    Collections.shuffle(shuffledUrls);
    this.playListQueue.clear();
    this.playListQueue.addAll(shuffledUrls);
    log.debug("Playlist queue shuffled");
  }

  public int getQueueSize() {
    return this.queue.size();
  }

  public int getPlaylistQueueSize() {
    return this.playListQueue.size();
  }

  public boolean hasPlaylist() {
    return !this.playListQueue.isEmpty();
  }
}
