# StreamTV (Android TV)

A native Android TV streaming application built with **Kotlin**, **Jetpack Compose for TV**, and **AndroidX Media3 (ExoPlayer)**.

---

## Architecture Overview

```text
StreamTV/
├── app/                  # Android TV Application (Leanback UI, TV Navigation, Player HUD)
├── core/
│   ├── model/           # Typed MediaItem, ContentType, StreamResult models
│   ├── player/          # Media3 ExoPlayer wrapper & D-Pad remote controller
│   └── network/         # Coroutine HTTP client supporting remote & local files
└── providers/
    ├── api/             # MetadataProvider & StreamProvider interfaces
    ├── demo/            # Known-good test streams & fallback catalog
    ├── metadata-tmdb/   # TheMovieDatabase (TMDB) discovery, search, genres & posters
    └── live-tv-m3u/     # M3U playlist parser, EPG attribute extraction & live stream resolver
```

### Key Principles

1. **Decoupled Metadata vs Playback**:
   - `MetadataProvider`: Fetches titles, overviews, ratings, genres, and posters (e.g., TMDB).
   - `StreamProvider`: Resolves playable media URLs (`.m3u8`, `.mpd`, direct stream).
2. **Typed Media Models**:
   - `MediaItem` stores `id`, `title`, `overview`, `posterUrl`, `contentType` (`MOVIE`, `SERIES`, `LIVE_TV`, `SPORTS`), season/episode numbers, and M3U metadata (`groupTitle`, `tvgId`, `tvgLogo`).
3. **10-Foot TV Experience**:
   - Every surface is optimized for D-Pad focus with high-contrast borders and subtle scaling.
   - Remote control support:
     - `DPAD_CENTER` / `SPACE`: Toggle Play / Pause
     - `DPAD_RIGHT`: Fast-Forward +10 seconds
     - `DPAD_LEFT`: Rewind -10 seconds
     - `BACK`: Close player and release resources cleanly
4. **State Management**:
   - Explicit Handling for **Loading**, **Empty**, **Error with Retry**, and **Invalid Stream** states across all screens.
5. **Compliance & Safety**:
   - No DRM bypass, no scraping of subscription services, and no unauthorized copyrighted streams are bundled.

---

## Configuration

### 1. TMDB API Key Setup
To enable real TMDB movie and TV discovery:
1. Register for a free API key at [themoviedb.org](https://www.themoviedb.org/settings/api).
2. Copy `local.properties.example` to `local.properties`:
   ```bash
   cp local.properties.example local.properties
   ```
3. Set your API key in `local.properties`:
   ```properties
   tmdb.api.key=your_tmdb_api_key_here
   ```
   *(Alternatively, export the environment variable: `export TMDB_API_KEY="your_key"`)*
4. `local.properties` is explicitly ignored by git to keep your private key out of source control.
5. If unconfigured, StreamTV automatically falls back to the demo catalog with clear configuration status in **Settings**.

### 2. M3U Live Playlist Configuration
StreamTV supports loading custom M3U/M3U8 playlists:
* **Supported Formats:** Standard `#EXTM3U` playlists with `#EXTINF` directives, `tvg-id`, `tvg-name`, `tvg-logo`, `group-title`, and stream URLs (`.m3u8`, `.mpd`, `.mp4`).
* **Remote URLs:** Set any valid HTTP/HTTPS playlist URL (e.g., public IPTV playlists from [iptv-org](https://github.com/iptv-org/iptv)).
* **Local Files:** Set local file paths (e.g. `file:///storage/emulated/0/Download/channels.m3u` or `/path/to/playlist.m3u`).
* **Sample Test Playlist:** A pre-configured sample verified playlist is available directly in the **Settings** screen.

---

## Running Unit Tests

To run the unit test suite verifying TMDB response parsing, M3U playlist parsing, and provider error resolution:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew test
```

Specific test classes:
* `TmdbResponseParserTest`: Validates trending movies, TV shows, genres, pagination, and error responses.
* `M3uParserTest`: Validates playlist parsing, channel group attribution, directive headers, malformed entries, and stream resolution errors.

---

## Building and Running the App

### In Android Studio
1. Launch **Android Studio**.
2. Click **Open** and select the `StreamTV` directory.
3. Allow Gradle to sync.
4. Select an **Android TV (1080p or 4K)** device or emulator.
5. Click **Run (`Shift + F10`)**.

### Command Line (ADB)
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.streamtv.app -c android.intent.category.LAUNCHER 1
```
