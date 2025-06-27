package com.lm_pakkanen.radio_pele_java.controllers;

import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import com.lm_pakkanen.radio_pele_java.models.Store;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.CurrentSongEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.QueueEmptyEmbed;
import com.lm_pakkanen.radio_pele_java.util.LavaLinkUtil;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Log4j2
@Component
public final class TrackScheduler {

  private static final List<String> PLAYLIST_URI_MATCHERS = List
      .of("/playlist/", "/album/", "?list=", "&list=");

  private final Store store;
  private final TidalController tidalController;
  private final SpotifyController spotifyController;
  private final TrackResolver trackResolver;

  private @Nullable Link link;
  private @Nullable TextChannel lastTextChan;

  public TrackScheduler(Store store, TidalController tidalController,
      SpotifyController spotifyController) {

    this.store = store;
    this.tidalController = tidalController;
    this.spotifyController = spotifyController;
    this.trackResolver = new TrackResolver(this.tidalController,
        this.spotifyController);
  }

  public void onTrackEnd(AudioTrackEndReason endReason) {
    log.info("Track ended");
    this.onTrackEndHandler(endReason);
  }

  /**
   * @return whether if the audio player is currently playing a track.
   */
  public boolean isPlaying() {
    return LavaLinkUtil.getPlayer(link).getTrack() != null;
  }

  /**
   * Start the audio player.
   * 
   * @return boolean whether the action succeeeded.
   */
  public boolean play() {
    if (!this.isPlaying()) {
      // If not yet playing a song, play the next one in the queue
      this.playNextTrack();
      return true;
    }

    return false;
  }

  /**
   * Tries to add the given URL to the queue. If the action fails, throws a
   * FailedToLoadSongException.
   * 
   * @param textChan       TextChannel instance where the command was invoked.
   * @param url            the URL of the song to add to the queue.
   * @param blockPlaylists
   * @return boolean whether the action succeeeded.
   * @throws FailedToLoadSongException
   */
  public Track addToQueue(TextChannel textChan, Link link, @Nullable String url,
      boolean blockPlaylists) throws FailedToLoadSongException {

    log.info("Adding song to queue");

    if (textChan != null) {
      log.info("Setting text channel");
      this.setLastTextChannel(textChan);
    } else {
      log.info("Text channel is null");
    }

    log.info("Setting link");
    this.link = link;

    if (url == null || url.isEmpty()) {
      throw new FailedToLoadSongException("Invalid url.");
    }

    final boolean asPlaylist = !blockPlaylists
        && PLAYLIST_URI_MATCHERS.stream().anyMatch(url::contains);

    final List<Track> audioTracks = this.trackResolver.resolve(link, url,
        asPlaylist);

    final Track firstTrack = audioTracks.getFirst();

    if (firstTrack == null) {
      throw new FailedToLoadSongException("Not found.");
    }

    if (asPlaylist) {
      log.info("Adding to queue as playlist");
      this.store.addPlaylist(audioTracks);
    } else {
      log.info("Adding to queue as single track");
      final boolean addResult = this.store.add(Optional.of(firstTrack));

      if (!addResult) {
        throw new FailedToLoadSongException("Not found.");
      }
    }

    return firstTrack;
  }

  /**
   * Skips the current song (stops the track and starts the next one in the
   * queue).
   * 
   * @return boolean whether the action succeeeded.
   */
  public Optional<Track> skipCurrentSong() {
    log.info("Skipping current song");
    stopCurrentSong();
    log.info("Playing the next track");
    return this.playNextTrack();
  }

  /**
   * Stops the audio player and clears the queue. Sets the last text channel to
   * null.
   * 
   * @return boolean whether the action succeeeded.
   */
  public boolean destroy() {
    log.info("Destroying track scheduler");
    stopCurrentSong();
    this.store.clear();
    this.store.clearPlaylist();
    this.lastTextChan = null;
    return true;
  }

  /**
   * Shuffles the current queue.
   * 
   * @return boolean whether the action succeeeded.
   */
  public boolean shuffle() {
    log.info("Shuffling queue");
    this.store.shuffle();
    return true;
  }

  /**
   * Handler for when a track ends. If the track ended normally, tries to start
   * the next track if the queue is not empty. Sends message to the latest text
   * channel informing users about the next song or abou the queue being empty.
   * 
   * @param endReason the reason the track ended. Supplied by superclass.
   */
  private void onTrackEndHandler(AudioTrackEndReason endReason) {

    if (endReason.equals(AudioTrackEndReason.LOAD_FAILED)) {
      log.warn("Could not load next song: '{}'.", endReason);
      return;
    }

    if (!endReason.getMayStartNext()) {
      log.warn("Track end handler may not start next song.");
      return;
    }

    log.info("Starting next song");

    final Optional<Track> nextTrackOpt = this.playNextTrack();

    if (nextTrackOpt.isEmpty() && this.lastTextChan != null) {
      MailMan.send(Optional.of(this.lastTextChan),
          new QueueEmptyEmbed().getEmbed());
      return;
    } else if (nextTrackOpt.isEmpty()) {
      log.info("Next track is null.");
      return;
    }

    final Track nextTrack = nextTrackOpt.get();

    if (this.lastTextChan != null) {
      MailMan.send(Optional.of(this.lastTextChan),
          new CurrentSongEmbed(nextTrack, this.store).getEmbed());
    } else {
      log.warn("Last text channel is null.");
    }
  }

  /**
   * Plays the next track in the queue if available.
   */
  private Optional<Track> playNextTrack() {

    log.info("Playing next track");
    if (this.store.getQueueSize() > 0 || !this.store.hasPlaylist()) {
      log.info("Playing next track from normal queue");

      if (this.store.hasPlaylist()) {
        log.info("Clearing playlist queue");
        this.store.clearPlaylist();
      }

      final Optional<Track> nextTrackOpt = this.store.shift();

      nextTrackOpt.ifPresentOrElse(nextTrack -> {
        log.info("Found track '{}'", nextTrack.getInfo().getTitle());
        LavaLinkUtil.getPlayer(link).setTrack(nextTrack).subscribe();
      }, () -> log.info("No track found"));

      return nextTrackOpt;
    }

    log.info("Playing next track from playlist queue");
    final Optional<Track> nextTrackOpt = this.store.shiftPlaylist();

    nextTrackOpt.ifPresentOrElse(nextTrack -> {
      log.info("Found track '{}'", nextTrack.getInfo().getTitle());
      LavaLinkUtil.getPlayer(link).setTrack(nextTrack).subscribe();
    }, () -> log.info("No track found"));

    return nextTrackOpt;
  }

  private void stopCurrentSong() {
    log.info("Stopping current song, if one is playing");
    LavaLinkUtil.getPlayer(link).setPaused(false).setTrack(null).subscribe();
  }

  private void setLastTextChannel(TextChannel textChan) {
    log.info("Setting last text channel to '{}'", textChan.getName());
    this.lastTextChan = textChan;
  }
}
