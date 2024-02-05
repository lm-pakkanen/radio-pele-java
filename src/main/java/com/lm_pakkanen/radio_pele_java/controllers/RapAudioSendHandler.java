package com.lm_pakkanen.radio_pele_java.controllers;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public class RapAudioSendHandler implements AudioSendHandler {

  private final TrackScheduler trackScheduler;
  private final AudioPlayer audioPlayer;

  private final ByteBuffer audioBuffer;
  private final MutableAudioFrame audioFrame;

  public RapAudioSendHandler(TrackScheduler trackScheduler) {
    this.trackScheduler = trackScheduler;
    this.audioPlayer = this.trackScheduler.getAudioPlayer();

    this.audioBuffer = ByteBuffer.allocate(2048);
    this.audioFrame = new MutableAudioFrame();
    this.audioFrame.setBuffer(this.audioBuffer);
  }

  @Override
  public boolean canProvide() {
    return this.audioPlayer.provide(this.audioFrame);
  }

  @Override
  public ByteBuffer provide20MsAudio() {
    this.audioBuffer.flip();
    return this.audioBuffer;
  }

  @Override
  public boolean isOpus() {
    return true;
  }

}
