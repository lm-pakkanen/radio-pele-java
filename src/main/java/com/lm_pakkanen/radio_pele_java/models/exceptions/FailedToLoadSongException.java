package com.lm_pakkanen.radio_pele_java.models.exceptions;

import org.springframework.lang.NonNull;

public final class FailedToLoadSongException extends Exception {
  public FailedToLoadSongException() {
    super("Failed to load song!");
  }

  public FailedToLoadSongException(@NonNull String message) {
    super("Failed to load song: " + message);
  }
}
