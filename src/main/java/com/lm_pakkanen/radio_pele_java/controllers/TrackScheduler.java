package com.lm_pakkanen.radio_pele_java.controllers;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.lm_pakkanen.radio_pele_java.models.Store;
import com.lm_pakkanen.radio_pele_java.models.exceptions.FailedToLoadSongException;
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
  private final Store store;

  private final AudioPlayerManager audioPlayerManager;
  private final AudioPlayer audioPlayer;

  private final TrackResolver trackResolver;
  private final RapAudioSendHandler rapAudioSendHandler;
  private final RapAudioEventHandler rapAudioEventHandler;

  private @Nullable TextChannel lastTextChan;

  public TrackScheduler() {
    this.store = new Store();

    this.audioPlayerManager = new DefaultAudioPlayerManager();

    this.audioPlayerManager
        .setFrameBufferDuration(TrackScheduler.FRAME_BUFFER_DURATION_MS);

    this.audioPlayer = audioPlayerManager.createPlayer();
    AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);

    this.trackResolver = new TrackResolver(this.audioPlayerManager);
    this.rapAudioSendHandler = new RapAudioSendHandler(this);
    this.rapAudioEventHandler = new RapAudioEventHandler(this);

    this.audioPlayer.addListener(this.rapAudioEventHandler);
  }

  public @NonNull AudioPlayer getAudioPlayer() {
    AudioPlayer audioPlayer = this.audioPlayer;

    if (audioPlayer == null) {
      throw new NullPointerException("AudioPlayer is null");
    }

    return audioPlayer;
  }

  public @NonNull RapAudioSendHandler getRapAudioSendHandler() {
    RapAudioSendHandler rapAudioSendHandler = this.rapAudioSendHandler;

    if (rapAudioSendHandler == null) {
      throw new NullPointerException("RapAudioSendHandler is null");
    }

    return rapAudioSendHandler;
  }

  public boolean play() {
    if (!this.isPlaying()) {
      // If not yet playing a song, play the next one in the queue
      this.playNextTrack();
      return true;
    }

    return false;
  }

  public void onTrackEndHandler(AudioPlayer player, AudioTrack track,
      AudioTrackEndReason endReason) {
    if (!endReason.mayStartNext) {
      return;
    }

    AudioTrack nextTrack = this.store.shift();

    if (nextTrack == null && lastTextChan != null) {
      MailMan.sendMessage(lastTextChan, "Q empty.");
      return;
    } else if (nextTrack == null) {
      return;
    }

    this.audioPlayer.playTrack(nextTrack);

    if (lastTextChan != null) {
      MailMan.sendMessage(lastTextChan, "Starting next song in queue.");
    }
  }

  public boolean addToQueue(@NonNull TextChannel textChan, String url)
      throws FailedToLoadSongException {
    this.setLastTextChannel(textChan);

    if (url == null || url.isEmpty()) {
      throw new FailedToLoadSongException("Invalid url.");
    }

    AudioTrack audioTrack = this.trackResolver.resolve(url);
    this.store.add(audioTrack);
    return true;
  }

  public boolean skipCurrentSong() {
    this.audioPlayer.stopTrack();
    this.playNextTrack();
    return true;
  }

  public boolean destroy() {
    this.audioPlayer.stopTrack();
    this.store.clear();
    this.lastTextChan = null;
    return true;
  }

  public boolean shuffle() {
    this.store.shuffle();
    return true;
  }

  public boolean isPlaying() {
    final AudioTrack currentTrack = this.audioPlayer.getPlayingTrack();
    final boolean isPlaying = currentTrack != null
        && !this.audioPlayer.isPaused();
    return isPlaying;
  }

  private void playNextTrack() {
    final AudioTrack nextTrack = this.store.shift();

    if (nextTrack != null) {
      this.audioPlayer.playTrack(nextTrack);
    }
  }

  private void setLastTextChannel(@NonNull TextChannel textChan) {
    this.lastTextChan = textChan;
  }
}
