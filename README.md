# Radio Pele

This repository contains the source code for a Discord music bot which supports playing audio from multiple different
streaming sources. It uses Lavalink as a backend. See [Lavalink Docs](https://lavalink.dev).

Uses [Lavalink Youtube Source plugin](https://github.com/lavalink-devs/youtube-source) to override the default YouTube
source. This version should be kept up to date because it often fixes roadblocks laid out by YouTube.

This bot was originally written with TypeScript -- this is a complete rewrite of it using Java / Spring Boot.

# Running the bot

This bot can be run in two ways:

1. Build and run with Docker compose (`./run.sh`).
2. Pull a ready-built image and run (`./run-artifactory.sh`). This requires authentication to the GCP artifactory.

Additionally, `run-lavalink.sh` can be used to run just the Lavalink instance.

If OAUTH is to be used, create the file `.env` in the root directory of the project with the following
contents:

```dotenv
PLUGINS_YOUTUBE_OAUTH_REFRESH_TOKEN="<your-refresh-token>"
```

The refresh token can be obtained by running the bot and following the OAUTH flow instructions in the std output.

# Playing songs

Songs can be played from most streaming sources or via direct links to video- or audio files.
Spotify links are supported such that the song is searched and played from YouTube.

# Playing playlists

Playlists are supported in much the same way as singles. They are however capped to a certain maximum length as not to
overwhelm the bot. If a song is queued while a playlist exists, the song is played after the current song and the
playlist is cleared.

# License

This project is licensed under the terms of the MIT license.
