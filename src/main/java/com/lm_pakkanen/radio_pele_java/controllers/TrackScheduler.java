package com.lm_pakkanen.radio_pele_java.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Component
public final class TrackScheduler {

  private static final int FRAME_BUFFER_DURATION_MS = 30_000;

  private final @NonNull SpotifyController spotifyController;
  private final @NonNull Store store;

  private final @NonNull AudioPlayerManager audioPlayerManager;
  private final @NonNull AudioPlayer audioPlayer;

  private final @NonNull TrackResolver trackResolver;
  private final @NonNull RapAudioSendHandler rapAudioSendHandler;
  private final @NonNull RapAudioEventHandler rapAudioEventHandler;

  private @Nullable TextChannel lastTextChan;

  public TrackScheduler(@Autowired @NonNull SpotifyController spotifyController)
      throws NullPointerException {

    this.spotifyController = spotifyController;
    this.store = new Store();

    this.audioPlayerManager = new DefaultAudioPlayerManager();
    this.audioPlayerManager
        .setFrameBufferDuration(TrackScheduler.FRAME_BUFFER_DURATION_MS);

    AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);

    AudioPlayer audioPlayer = this.audioPlayerManager.createPlayer();

    if (audioPlayer == null) {
      throw new NullPointerException("AudioPlayer is null");
    }

    this.audioPlayer = audioPlayer;

    this.trackResolver = new TrackResolver(this.spotifyController,
        this.audioPlayerManager);

    this.rapAudioSendHandler = new RapAudioSendHandler(this);
    this.rapAudioEventHandler = new RapAudioEventHandler(this);

    this.audioPlayer.addListener(this.rapAudioEventHandler);
  }

  /**
   * @return store instance.
   */
  public @NonNull Store getStore() {
    return this.store;
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
    if (!endReason.mayStartNext) {
      return;
    }

    final AudioTrack nextTrack = this.playNextTrack();

    if (nextTrack == null && lastTextChan != null) {
      MailMan.sendEmbed(lastTextChan, new QueueEmptyEmbed().getEmbed());
      return;
    } else if (nextTrack == null) {
      return;
    }

    this.audioPlayer.playTrack(nextTrack);

    if (lastTextChan != null) {
      MailMan.sendEmbed(lastTextChan,
          new CurrentSongEmbed(nextTrack, this.store).getEmbed());
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
   * @param textChan TextChannel instance where the command was invoked.
   * @param url      the URL of the song to add to the queue.
   * @return boolean whether the action succeeeded.
   * @throws FailedToLoadSongException
   */
  public @NonNull AudioTrack addToQueue(@NonNull TextChannel textChan,
      @Nullable String url) throws FailedToLoadSongException {
    this.setLastTextChannel(textChan);

    if (url == null || url.isEmpty()) {
      throw new FailedToLoadSongException("Invalid url.");
    }

    final boolean isPlaylist = url.contains("/playlist/")
        || url.contains("?list=") || url.contains("&list=");

    final List<AudioTrack> audioTracks = this.trackResolver.resolve(url,
        isPlaylist);

    final AudioTrack firstTrack = audioTracks.get(0);

    if (firstTrack == null) {
      throw new FailedToLoadSongException("Not found.");
    }

    if (isPlaylist) {
      this.store.addPlaylist(audioTracks);
    } else {
      for (AudioTrack audioTrack : audioTracks) {
        this.store.add(audioTrack);
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
    if (!this.store.hasPlaylist()) {
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
