package com.lm_pakkanen.radio_pele_java.controllers;

import java.nio.ByteBuffer;

import org.springframework.lang.NonNull;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public class RapAudioSendHandler implements AudioSendHandler {

  private final @NonNull AudioPlayer audioPlayer;

  private final @NonNull ByteBuffer audioBuffer;
  private final @NonNull MutableAudioFrame audioFrame;

  /**
   * @throws NullPointerException
   */
  public RapAudioSendHandler(@NonNull AudioPlayer audioPlayer)
      throws NullPointerException {
    this.audioPlayer = audioPlayer;

    final ByteBuffer audioBuffer = ByteBuffer.allocate(2048);

    if (audioBuffer == null) {
      throw new NullPointerException("AudioBuffer is null");
    }

    this.audioBuffer = audioBuffer;
    this.audioFrame = new MutableAudioFrame();
    this.audioFrame.setBuffer(this.audioBuffer);
  }

  @Override
  public boolean canProvide() {
    return this.audioPlayer.provide(this.audioFrame);
  }

  @Override
  public @NonNull ByteBuffer provide20MsAudio() {
    this.audioBuffer.flip();
    return this.audioBuffer;
  }

  @Override
  public boolean isOpus() {
    return true;
  }
}
