package com.lm_pakkanen.radio_pele_java.models.exceptions;

public final class SongNotFoundException extends Exception {
  public SongNotFoundException() {
    super("Song not found!");
  }
}
