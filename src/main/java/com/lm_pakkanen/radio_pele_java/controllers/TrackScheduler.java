package com.lm_pakkanen.radio_pele_java.controllers;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import com.lm_pakkanen.radio_pele_java.models.Store;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.CurrentSongEmbed;
import com.lm_pakkanen.radio_pele_java.models.message_embeds.QueueEmptyEmbed;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Log4j2
@Component
@Lazy
public final class TrackScheduler extends AudioEventAdapter {

  private static final int FRAME_BUFFER_DURATION_MS = 30_000;

  private static final List<String> PLAYLIST_URI_MATCHERS = List
      .of("/playlist/", "/album/", "?list=", "&list=");

  private final @NonNull TidalController tidalController;
  private final @NonNull SpotifyController spotifyController;
  private final @NonNull Store store;

  private final @NonNull AudioPlayerManager audioPlayerManager;
  private final @NonNull AudioPlayer audioPlayer;

  private final @NonNull TrackResolver trackResolver;
  private final @NonNull RapAudioSendHandler rapAudioSendHandler;

  private @Nullable TextChannel lastTextChan;

  public TrackScheduler(@Autowired @NonNull TidalController tidalController,
      @Autowired @NonNull SpotifyController spotifyController,
      @Autowired @NonNull Store store) {

    this.tidalController = tidalController;
    this.spotifyController = spotifyController;
    this.store = store;

    this.audioPlayerManager = new DefaultAudioPlayerManager();

    final AudioPlayer audioPlayer = this.audioPlayerManager.createPlayer();

    if (audioPlayer == null) {
      log.error("AudioPlayer is null.");
      throw new IllegalStateException("AudioPlayer is null");
    }

    this.audioPlayer = audioPlayer;

    this.trackResolver = new TrackResolver(this.tidalController,
        this.spotifyController, this.audioPlayerManager);

    this.rapAudioSendHandler = new RapAudioSendHandler(this.getAudioPlayer());

    this.audioPlayerManager
        .setFrameBufferDuration(TrackScheduler.FRAME_BUFFER_DURATION_MS);
    this.audioPlayerManager
        .registerSourceManager(new YoutubeAudioSourceManager());

    AudioSourceManagers.registerRemoteSources(this.audioPlayerManager,
        com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);

    this.audioPlayer.addListener(this);
  }

  /**
   * @throws NullPointerException
   */
  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track,
      AudioTrackEndReason endReason) throws NullPointerException {
    log.info("Track ended");

    if (player == null) {
      throw new NullPointerException("Player is null");
    }

    if (track == null) {
      throw new NullPointerException("Track is null");
    }

    this.onTrackEndHandler(player, track, endReason);
  }

  /**
   * @return AudioPlayer instance.
   */
  public @NonNull AudioPlayer getAudioPlayer() {
    final AudioPlayer audioPlayer = this.audioPlayer;
    return audioPlayer;
  }

  /**
   * @return RapAudioSendHandler instance.
   */
  public @NonNull RapAudioSendHandler getRapAudioSendHandler() {
    return this.rapAudioSendHandler;
  }

  /**
   * Handler for when a track ends. If the track ended normally, tries to start
   * the next track if the queue is not empty. Sends message to the latest text
   * channel informing users about the next song or abou the queue being empty.
   * 
   * @param player    AudioPlayer instance. Supplied by superclass.
   * @param track     the track that ended. Supplied by superclass.
   * @param endReason the reason the track ended. Supplied by superclass.
   */
  public void onTrackEndHandler(@NonNull AudioPlayer player,
      @NonNull AudioTrack track, AudioTrackEndReason endReason) {

    if (endReason.equals(AudioTrackEndReason.LOAD_FAILED)) {
      log.warn("Could not load next song: '{}'.", endReason);
      return;
    }

    if (!endReason.mayStartNext) {
      log.warn("Track end handler may not start next song.");
      return;
    }

    log.info("Starting next song");

    final Optional<AudioTrack> nextTrackOpt = this.playNextTrack();

    if (nextTrackOpt.isEmpty() && this.lastTextChan != null) {
      MailMan.send(Optional.of(this.lastTextChan),
          new QueueEmptyEmbed().getEmbed());
      return;
    } else if (nextTrackOpt.isEmpty()) {
      log.info("Next track is null.");
      return;
    }

    final AudioTrack nextTrack = nextTrackOpt.get();

    if (this.lastTextChan != null) {
      MailMan.send(Optional.of(this.lastTextChan),
          new CurrentSongEmbed(nextTrack, this.store).getEmbed());
    } else {
      log.warn("Last text channel is null.");
    }
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
  public AudioTrack addToQueue(TextChannel textChan, @Nullable String url,
      boolean blockPlaylists) throws FailedToLoadSongException {
    log.info("Adding song to queue");

    if (textChan != null) {
      log.info("Setting text channel");
      this.setLastTextChannel(textChan);
    } else {
      log.info("Text channel is null");
    }

    if (url == null || url.isEmpty()) {
      throw new FailedToLoadSongException("Invalid url.");
    }

    final boolean asPlaylist = !blockPlaylists
        && PLAYLIST_URI_MATCHERS.stream().anyMatch(url::contains);

    final AudioTrack[] audioTracks = this.trackResolver.resolve(url,
        asPlaylist);

    final AudioTrack firstTrack = audioTracks[0];

    if (firstTrack == null) {
      throw new FailedToLoadSongException("Not found.");
    }

    if (asPlaylist) {
      log.info("Adding to queue as playlist");
      this.store.addPlaylist(audioTracks);
    } else {
      log.info("Adding to queue as single track");
      final boolean addResult = this.store.add(Optional.of(firstTrack));
      if (addResult == false) {
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
  public Optional<AudioTrack> skipCurrentSong() {
    log.info("Skipping current song");
    log.info("Stopping current song, if one is playing");
    this.audioPlayer.stopTrack();
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
    this.audioPlayer.stopTrack();
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
   * @return whether if the audio player is currently playing a track.
   */
  public boolean isPlaying() {
    final AudioTrack currentTrack = this.audioPlayer.getPlayingTrack();
    final boolean isPlaying = currentTrack != null
        && !this.audioPlayer.isPaused();
    return isPlaying;
  }

  /**
   * Plays the next track in the queue if available.
   */
  private Optional<AudioTrack> playNextTrack() {
    log.info("Playing next track");
    if (this.store.getQueueSize() > 0 || !this.store.hasPlaylist()) {
      log.info("Playing next track from normal queue");

      if (this.store.hasPlaylist()) {
        log.info("Clearing playlist queue");
        this.store.clearPlaylist();
      }

      final Optional<AudioTrack> nextTrackOpt = this.store.shift();

      if (nextTrackOpt.isPresent()) {
        final AudioTrack nextTrack = nextTrackOpt.get();
        log.info("Found track '{}'", nextTrack.getInfo().title);
        this.audioPlayer.playTrack(nextTrack);
      } else {
        log.info("No track found");
      }

      return nextTrackOpt;
    }

    log.info("Playing next track from playlist queue");
    final Optional<AudioTrack> nextTrackOpt = this.store.shiftPlaylist();

    if (nextTrackOpt.isPresent()) {
      final AudioTrack nextTrack = nextTrackOpt.get();
      log.info("Found track '{}'", nextTrack.getInfo().title);
      this.audioPlayer.playTrack(nextTrack);
    } else {
      log.info("No track found");
    }

    return nextTrackOpt;
  }

  /**
   * @param textChan last text channel where a command was invoked.
   */
  private void setLastTextChannel(@NonNull TextChannel textChan) {
    log.info("Setting last text channel to '{}'", textChan.getName());
    this.lastTextChan = textChan;
  }
}
