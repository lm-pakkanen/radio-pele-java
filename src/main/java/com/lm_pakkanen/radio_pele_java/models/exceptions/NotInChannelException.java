package com.lm_pakkanen.radio_pele_java.models.exceptions;

public final class NotInChannelException extends Exception {
  public NotInChannelException() {
    super("Not in a voice channel!");
  }
}
