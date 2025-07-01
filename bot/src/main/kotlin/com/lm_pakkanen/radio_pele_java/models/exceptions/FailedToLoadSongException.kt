package com.lm_pakkanen.radio_pele_java.models.exceptions

class FailedToLoadSongException : Exception {
  constructor() : super("Failed to load song!")
  constructor(message: String) : super("Failed to load song: $message")
}