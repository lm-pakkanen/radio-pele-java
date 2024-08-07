package com.lm_pakkanen.radio_pele_java.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Component
@Lazy
public final class TrackScheduler extends AudioEventAdapter {

  private final static Logger logger = LoggerFactory
      .getLogger(TrackScheduler.class);

  private static final int FRAME_BUFFER_DURATION_MS = 30_000;

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
      @Autowired @NonNull Store store) throws NullPointerException {

    try {
      this.tidalController = tidalController;
      this.spotifyController = spotifyController;
      this.store = store;

      this.audioPlayerManager = new DefaultAudioPlayerManager();

      final AudioPlayer audioPlayer = this.audioPlayerManager.createPlayer();

      if (audioPlayer == null) {
        throw new NullPointerException("AudioPlayer is null");
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
    } catch (NullPointerException e) {
      logger.error("AudioPlayer is null.");
      throw e;
    }
  }

  /**
   * @throws NullPointerException
   */
  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track,
      AudioTrackEndReason endReason) throws NullPointerException {
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
    if (!endReason.mayStartNext
        || endReason.equals(AudioTrackEndReason.LOAD_FAILED)) {
      logger.warn("Could not load next song.");
      return;
    }

    final AudioTrack nextTrack = this.playNextTrack();

    if (nextTrack == null && this.lastTextChan != null) {
      MailMan.send(this.lastTextChan, new QueueEmptyEmbed().getEmbed());
      return;
    } else if (nextTrack == null) {
      logger.info("Next track is null.");
      return;
    }

    this.audioPlayer.playTrack(nextTrack);

    if (this.lastTextChan != null) {
      MailMan.send(this.lastTextChan,
          new CurrentSongEmbed(nextTrack, this.store).getEmbed());
    } else {
      logger.warn("Last text channel is null.");
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
  public @NonNull AudioTrack addToQueue(@NonNull TextChannel textChan,
      @Nullable String url, boolean blockPlaylists)
      throws FailedToLoadSongException {

    if (textChan != null) {
      this.setLastTextChannel(textChan);
    }

    if (url == null || url.isEmpty()) {
      throw new FailedToLoadSongException("Invalid url.");
    }

    final boolean asPlaylist = !blockPlaylists
        && (url.contains("/playlist/") || url.contains("/album/")
            || url.contains("?list=") || url.contains("&list="));

    final AudioTrack[] audioTracks = this.trackResolver.resolve(url,
        asPlaylist);

    final AudioTrack firstTrack = audioTracks[0];

    if (firstTrack == null) {
      throw new FailedToLoadSongException("Not found.");
    }

    if (asPlaylist) {
      this.store.addPlaylist(audioTracks);
    } else {
      for (AudioTrack audioTrack : audioTracks) {
        final boolean addResult = this.store.add(audioTrack);

        if (addResult == false) {
          throw new FailedToLoadSongException("Not found.");
        }

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
  public @Nullable AudioTrack skipCurrentSong() {
    this.audioPlayer.stopTrack();
    return this.playNextTrack();
  }

  /**
   * Stops the audio player and clears the queue. Sets the last text channel to
   * null.
   * 
   * @return boolean whether the action succeeeded.
   */
  public boolean destroy() {
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
  private @Nullable AudioTrack playNextTrack() {
    if (this.store.getQueueSize() > 0 || !this.store.hasPlaylist()) {
      if (this.store.hasPlaylist()) {
        this.store.clearPlaylist();
      }

      final AudioTrack nextTrack = this.store.shift();

      if (nextTrack != null) {
        this.audioPlayer.playTrack(nextTrack);
      }

      return nextTrack;
    }

    final AudioTrack nextTrack = this.store.shiftPlaylist();

    if (nextTrack != null) {
      this.audioPlayer.playTrack(nextTrack);
    }

    return nextTrack;
  }

  /**
   * @param textChan last text channel where a command was invoked.
   */
  private void setLastTextChannel(@NonNull TextChannel textChan) {
    this.lastTextChan = textChan;
  }
}
