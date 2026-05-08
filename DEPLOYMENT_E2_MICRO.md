# Deployment Guide for GCP E2-Micro Instance

This guide covers optimizing the Radio Pele Java bot for deployment on GCP's E2-micro instance (1GB RAM).

## Overview

The E2-micro instance is a very resource-constrained environment:
- **CPU:** 0.25-0.5 vCPU (shared)
- **RAM:** 1 GB
- **Storage:** 20-100 GB

The previous configuration crashed the bot regularly due to excessive memory allocation.

## Key Improvements Made

### 1. JVM Heap Memory Optimization

**Before (BROKEN):**
```dockerfile
ENV JAVA_TOOL_OPTIONS "-XX:MaxGCPauseMillis=200 -Xms1500m -Xmx1500m"
```

**After (FIXED):**
```dockerfile
ENV JAVA_TOOL_OPTIONS "-XX:+UseSerialGC -Xms128m -Xmx384m -XX:+ExitOnOutOfMemory"
```

**Why this matters:**
- Allocating 1500MB to a process on a 1GB instance causes Out of Memory (OOM) errors
- The Linux OOM killer terminates the process, which can freeze or crash the entire VM
- SerialGC is optimized for low-memory environments
- 384MB max heap leaves ~600MB for OS and Lavalink

### 2. Garbage Collection Selection

- **Before:** Default GC with unrealistic pause time goals
- **After:** `UseSerialGC` - optimized for single-threaded low-memory environments

**Why this matters:**
- Parallel GC and G1GC require more memory overhead and CPU
- Serial GC minimizes heap overhead and works better with 384MB allocation
- Reduces GC pauses and memory fragmentation

### 3. Lavalink Memory Configuration

**Before:**
```yaml
- _JAVA_OPTIONS=-Xmx4G
```

**After:**
```yaml
- _JAVA_OPTIONS=-XX:+UseSerialGC -Xms64m -Xmx128m
```

**Memory allocation for E2-micro:**
- Lavalink (container limit): 256MB
- Bot (container limit): 512MB
- OS/Docker overhead: ~200-250MB

### 4. Docker Memory Limits

Added explicit memory constraints to prevent one service consuming all RAM:

```yaml
lavalink:
  mem_limit: 256m
  memswap_limit: 256m

bot:
  mem_limit: 512m
  memswap_limit: 512m
```

**Why this matters:**
- Prevents OOM killer from terminating the entire system
- Ensures each service has predictable resource allocation
- Docker will gracefully kill a container if it exceeds limits instead of freezing the VM

### 5. Queue Memory Management

**Changes in Store.kt:**
- Initialize `playListQueue` with capacity 25 (matches `PLAYLIST_MAX_SIZE`)
- Changed `queue` from unbounded to `LinkedBlockingQueue(100)`

**Why this matters:**
- Prevents unbounded memory growth from queue operations
- ArrayList with proper capacity reduces reallocation overhead
- Bounded queue provides backpressure

## Monitoring and Debugging

### Check Memory Usage

```bash
# SSH into the VM
gcloud compute ssh your-instance --zone=your-zone

# Check container memory stats
docker stats

# Check JVM memory usage (inside container)
docker exec radio_pele_bot jps -lv

# View full JVM flags
docker exec radio_pele_bot java -XX:+PrintFlagsFinal -version | grep -i memory
```

### View Logs

```bash
# Bot logs
docker logs -f radio_pele_bot

# Lavalink logs
docker logs -f radio_pele_lavalink

# Check for OOM errors in system logs
sudo journalctl -u docker.service | grep -i oom
dmesg | grep -i oom  # Kernel OOM killer logs
```

### JVM Crash Indicators

Watch for these in logs:
- `OutOfMemoryError: Java heap space` - Heap is too small
- `OutOfMemoryError: GC overhead limit exceeded` - Application creating too much garbage
- `java.lang.RuntimeException: Cannot run program` - Not enough native memory
- Container exits with code 137 - OOM killer terminated it

## Performance Tuning

### If the bot still crashes or is slow:

1. **Check container limits:**
   ```bash
   docker stats
   # If LIMIT column shows 256m/512m, limits are working
   ```

2. **Reduce Lavalink further (if not heavily loaded):**
   ```yaml
   environment:
     - _JAVA_OPTIONS=-XX:+UseSerialGC -Xms32m -Xmx96m
   mem_limit: 192m
   ```

3. **Reduce bot heap (if stable with less):**
   ```dockerfile
   ENV JAVA_TOOL_OPTIONS "-XX:+UseSerialGC -Xms64m -Xmx256m -XX:+ExitOnOutOfMemory"
   ```

4. **Enable GC logging for diagnosis:**
   ```dockerfile
   ENV JAVA_TOOL_OPTIONS "-XX:+UseSerialGC -Xms128m -Xmx384m -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:/tmp/gc.log"
   ```

### Advanced Tuning (if needed):

```dockerfile
# Add to JAVA_TOOL_OPTIONS if experiencing pauses:
-XX:SurvivorRatio=8          # Reduce survivor space
-XX:NewRatio=3               # Adjust young/old generation ratio
-XX:+DisableExplicitGC       # Prevent external GC triggers
-XX:+AlwaysPreTouch=false    # Don't pre-allocate pages (saves memory)
```

## Troubleshooting

### Bot keeps crashing

1. Check Docker memory limits are enforced:
   ```bash
   docker inspect radio_pele_bot | grep -i memory
   ```

2. View recent memory pressure:
   ```bash
   cat /proc/meminfo
   free -h
   ```

3. If Lavalink won't start, reduce its heap:
   ```yaml
   - _JAVA_OPTIONS=-XX:+UseSerialGC -Xms32m -Xmx64m
   ```

### Slow playback or freezes

1. Check GC logs if enabled
2. Reduce JVM `MaxGCPauseMillis` target (if using G1GC)
3. Restart services to clear any memory leaks

### Out of Memory errors

1. Check what consumed memory:
   ```bash
   docker stats
   ps aux | grep java
   ```

2. Lower heap limits immediately
3. Check for memory leaks in logs (repeated "added playlist" without "cleared playlist")

## Production Deployment Checklist

- [ ] Verify compose.yml has memory limits for all services
- [ ] Confirm Dockerfile uses E2-micro-optimized JVM flags
- [ ] Test bot startup and basic operations locally
- [ ] Monitor memory usage for 24 hours after deployment
- [ ] Set up alerting for OOM events
- [ ] Document any custom JVM flags used
- [ ] Keep backup of working configuration

## References

- [GCP E2 Machine Family](https://cloud.google.com/compute/docs/machine-types#e2_machine_types)
- [Java Garbage Collection Tuning](https://www.oracle.com/technical-resources/articles/java/cg.html)
- [Docker Memory Resource Constraints](https://docs.docker.com/config/containers/resource_constraints/#memory)
- [SerialGC vs ParallelGC](https://docs.oracle.com/en/java/javase/17/gctuning/serial-collector.html)

## Contact & Support

If the bot continues to crash after these optimizations:

1. Collect 24-hour logs and memory statistics
2. Check if user load correlates with crashes
3. Consider upgrading to E2-standard or E2-medium instance
4. Profile application for memory leaks using async-profiler in container
