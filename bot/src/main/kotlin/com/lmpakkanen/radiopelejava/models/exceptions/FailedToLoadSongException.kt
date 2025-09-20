package com.lmpakkanen.radiopelejava.models.exceptions

class FailedToLoadSongException : Exception {
    constructor() : super("Failed to load song!")
    constructor(message: String) : super("Failed to load song: $message")
}
