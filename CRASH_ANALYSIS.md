# Radio Pele Java Bot - Crash Root Cause Analysis & Fixes

## Executive Summary

Your Discord bot crashes frequently on GCP's E2-micro instance because **the JVM heap memory allocation exceeds the total instance RAM by 50%**. This causes the Linux OOM (Out of Memory) killer to terminate processes or freeze the entire VM.

**Status:** ✅ **FIXED** - All critical issues identified and corrected.

---

## Root Causes Identified

### 🔴 CRITICAL: Excessive JVM Heap Allocation

**Problem:** `Dockerfile` allocated 1500MB heap on a 1GB instance
```dockerfile
# BEFORE (BROKEN)
ENV JAVA_TOOL_OPTIONS "-XX:MaxGCPauseMillis=200 -Xms1500m -Xmx1500m"
```

**Why it crashes:**
- E2-micro has 1GB total RAM
- JVM requested 1500MB (150% of available memory)
- OS needs ~200MB for kernel and services
- Result: Immediate Out of Memory → OOM Killer → Process termination or VM freeze

**Impact:** **CRITICAL** - This alone causes the crashes

---

### 🔴 CRITICAL: Lavalink Memory Misconfiguration

**Problem:** `compose.yml` allocated 4GB to Lavalink
```yaml
# BEFORE (BROKEN)
- _JAVA_OPTIONS=-Xmx4G
```

**Why it crashes:**
- 4GB requested on 1GB instance is impossible
- Lavalink fails to start or consumes all available memory
- Bot can't connect to audio service → crashes
- Memory contention between bot and Lavalink

**Impact:** **CRITICAL** - Prevents audio playback, cascading failure

---

### 🟠 HIGH: No Resource Limits

**Problem:** Docker containers had no memory limits
- Both services could consume all 1GB
- First service to allocate wins, second starves
- No graceful degradation

**Impact:** **HIGH** - Makes system unreliable

---

### 🟡 MEDIUM: Memory Leaks in Queue Management

**Problem:** `Store.kt` had suboptimal memory allocation
```kotlin
// BEFORE (INEFFICIENT)
val playListQueue: MutableList<Track> = ArrayList(1)  // Capacity 1, grows to 25!
val queue: BlockingQueue<Track> = LinkedBlockingQueue()  // UNBOUNDED!
```

**Why it matters:**
- ArrayList with capacity 1 reallocates continuously as playlists load
- Unbounded LinkedBlockingQueue can grow infinitely
- Track objects retain heavy metadata (audio data, URLs, metadata)

**Impact:** **MEDIUM** - Exacerbates memory pressure

---

### 🟡 MEDIUM: Suboptimal Garbage Collection

**Problem:** Using default GC with unrealistic pause time goals
```dockerfile
# BEFORE (POOR CHOICE FOR LOW-MEMORY)
-XX:MaxGCPauseMillis=200  # G1GC goal, requires more memory/CPU
```

**Why it matters:**
- Default GC (G1GC) optimized for large heaps with many cores
- E2-micro has 0.25-0.5 vCPU and 384MB heap
- Better choice: SerialGC (single-threaded, minimal overhead)

**Impact:** **MEDIUM** - Causes GC pauses, wastes memory

---

## Solutions Implemented

### ✅ Fix 1: Optimize Bot JVM Heap

**File:** `bot/Dockerfile`

```dockerfile
# AFTER (FIXED)
ENV JAVA_TOOL_OPTIONS "-XX:+UseSerialGC -Xms128m -Xmx384m -XX:+ExitOnOutOfMemory"
```

**Why this works:**
- `-XX:+UseSerialGC` → Single-threaded GC, minimal overhead
- `-Xms128m` → Start with 128MB (fast startup)
- `-Xmx384m` → Max 384MB, leaves 600MB for OS and Lavalink
- `-XX:+ExitOnOutOfMemory` → Graceful crash instead of zombie process

---

### ✅ Fix 2: Optimize Lavalink Memory

**File:** `compose.yml`

```yaml
# AFTER (FIXED)
environment:
  - _JAVA_OPTIONS=-XX:+UseSerialGC -Xms64m -Xmx128m
mem_limit: 256m
memswap_limit: 256m
```

**Why this works:**
- Lavalink limited to 256MB (actual limit + overhead)
- SerialGC for low-memory efficiency
- Can't exceed limit, Docker kills gracefully if it tries

---

### ✅ Fix 3: Add Docker Memory Limits

**File:** `compose.yml`

```yaml
services:
  bot:
    mem_limit: 512m
    memswap_limit: 512m
  lavalink:
    mem_limit: 256m
    memswap_limit: 256m
```

**Memory Budget for 1GB instance:**
```
Total Available:       1000 MB
├─ OS/Docker:        ~200 MB
├─ Lavalink limit:     256 MB
└─ Bot limit:          512 MB
└─ Buffer:             32 MB
```

**Why this works:**
- Docker enforces limits, can't exceed
- OOM killer targets container, not entire VM
- Predictable resource allocation

---

### ✅ Fix 4: Improve Queue Memory Management

**File:** `bot/src/main/kotlin/.../models/Store.kt`

```kotlin
// BEFORE
val playListQueue: MutableList<Track> = ArrayList(1)
val queue: BlockingQueue<Track> = LinkedBlockingQueue()

// AFTER
val playListQueue: MutableList<Track> = ArrayList(25)  // Match PLAYLIST_MAX_SIZE
val queue: BlockingQueue<Track> = LinkedBlockingQueue(100)  // Bounded queue
```

**Why this works:**
- ArrayList(25) matches expected size, no reallocation
- LinkedBlockingQueue(100) prevents unbounded growth
- Reduces memory fragmentation

---

### ✅ Fix 5: Add Defensive Error Handling

**File:** `TrackScheduler.kt`

Added try-catch around playlist/track addition to prevent resource leaks if operation fails.

```kotlin
try {
    if (asPlaylist) {
        this.store.addPlaylist(audioTracks)
    } else {
        val addResult = this.store.add(firstTrack)
        if (!addResult) {
            throw FailedToLoadSongException("Not found.")
        }
    }
} catch (ex: Exception) {
    logger.warn("Failed to add tracks to queue", ex)
    throw FailedToLoadSongException("Failed to add tracks: ${ex.message}")
}
```
