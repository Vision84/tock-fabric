package net.tockmod.chunk;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.tockmod.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ChunkFuseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tock/ChunkFuse");
    private final Map<ChunkPos, ChunkActivityData> chunkActivityMap = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupTime = new AtomicLong(0);
    private static final long CLEANUP_INTERVAL = 1000; // Cleanup every second

    public void onServerTickStart(MinecraftServer server) {
        if (!ModConfig.getInstance().chunkfuseEnabled) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        
        // Periodic cleanup of old chunks
        if (currentTime - lastCleanupTime.get() > CLEANUP_INTERVAL) {
            int beforeSize = chunkActivityMap.size();
            cleanupInactiveChunks(currentTime);
            int afterSize = chunkActivityMap.size();
            if (beforeSize != afterSize) {
                LOGGER.info("Cleaned up {} inactive chunks ({} remaining)", beforeSize - afterSize, afterSize);
            }
            lastCleanupTime.set(currentTime);

            // Log chunk activity summary
            logChunkActivitySummary();
        }
    }

    public void onServerTickEnd(MinecraftServer server) {
        // No-op for now
    }

    public void markChunkActive(ServerWorld world, ChunkPos pos) {
        if (!ModConfig.getInstance().chunkfuseEnabled) {
            return;
        }

        ChunkActivityData data = chunkActivityMap.computeIfAbsent(pos, 
            k -> new ChunkActivityData(world));
        
        data.updateLastActivity();
        LOGGER.debug("Marked chunk {} in {} as active", pos, world.getRegistryKey().getValue());
    }

    public boolean isChunkCold(ChunkPos pos) {
        if (!ModConfig.getInstance().chunkfuseEnabled) {
            return false;
        }

        ChunkActivityData data = chunkActivityMap.get(pos);
        if (data == null) {
            LOGGER.debug("Chunk {} has no activity data, considered cold", pos);
            return true;
        }

        long inactiveTime = System.currentTimeMillis() - data.lastActivity;
        boolean isCold = inactiveTime > ModConfig.getInstance().chunkColdTimeout * 1000;
        if (isCold) {
            LOGGER.debug("Chunk {} is cold (inactive for {}ms)", pos, inactiveTime);
        }
        return isCold;
    }

    private void logChunkActivitySummary() {
        LOGGER.info("=== Chunk Activity Summary ===");
        LOGGER.info("Total active chunks: {}", chunkActivityMap.size());
        
        // Group chunks by world
        Map<ServerWorld, Long> chunksByWorld = chunkActivityMap.entrySet().stream()
            .collect(java.util.stream.Collectors.groupingBy(
                entry -> entry.getValue().world,
                java.util.stream.Collectors.counting()));
        
        chunksByWorld.forEach((world, count) -> 
            LOGGER.info("  {}: {} chunks", world.getRegistryKey().getValue(), count));
        
        // Count hot vs cold chunks
        long hotChunks = chunkActivityMap.values().stream()
            .filter(data -> data.isHot())
            .count();
        
        LOGGER.info("Hot chunks: {}", hotChunks);
        LOGGER.info("Cold chunks: {}", chunkActivityMap.size() - hotChunks);
    }

    private void cleanupInactiveChunks(long currentTime) {
        chunkActivityMap.entrySet().removeIf(entry -> {
            ChunkActivityData data = entry.getValue();
            boolean isInactive = currentTime - data.getLastActivity() > 
                ModConfig.getInstance().chunkColdTimeout * 1000;
            
            if (isInactive) {
                LOGGER.debug("Removing inactive chunk {} in {}", 
                    entry.getKey(), 
                    data.world.getRegistryKey().getValue());
            }
            
            return isInactive;
        });
    }

    private static class ChunkActivityData {
        private final ServerWorld world;
        private long lastActivity;

        public ChunkActivityData(ServerWorld world) {
            this.world = world;
            this.lastActivity = System.currentTimeMillis();
        }

        public void updateLastActivity() {
            this.lastActivity = System.currentTimeMillis();
        }

        public long getLastActivity() {
            return lastActivity;
        }

        public boolean isHot() {
            return System.currentTimeMillis() - lastActivity < 
                ModConfig.getInstance().chunkColdTimeout * 1000;
        }
    }
} 