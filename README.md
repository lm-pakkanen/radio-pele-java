# Radio Pele

This repository contains the source code for a Discord music bot which supports playing audio from multiple different streaming sources.
This bot was originally written with TypeScript -- this is a complete rewrite of it using Java / Spring Boot.

# Playing songs

Songs can be played from most streaming sources or via direct links to video- or audiofiles.
Spotify links are supported such that the song is searched and played from YouTube.

# Playing playlists

Playlists are supported in much the same way as singles.
They are however capped to a certain maximum length as not to overwhelm the bot.
If a song is queued while a playlist exists, the song is played after the current song and the playlist is cleared.

# License

This project is licensed under the terms of the MIT license.
