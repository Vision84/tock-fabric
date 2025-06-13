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
            cleanupInactiveChunks(currentTime);
            lastCleanupTime.set(currentTime);
        }
    }

    public void onServerTickEnd(MinecraftServer server) {
        // No-op for now
    }

    public void markChunkActive(ServerWorld world, ChunkPos pos) {
        if (!ModConfig.getInstance().chunkfuseEnabled) {
            return;
        }

        chunkActivityMap.compute(pos, (key, data) -> {
            if (data == null) {
                return new ChunkActivityData(System.currentTimeMillis());
            }
            data.lastActivityTime = System.currentTimeMillis();
            return data;
        });
    }

    public boolean isChunkCold(ChunkPos pos) {
        if (!ModConfig.getInstance().chunkfuseEnabled) {
            return false;
        }

        ChunkActivityData data = chunkActivityMap.get(pos);
        if (data == null) {
            return true;
        }

        long inactiveTime = System.currentTimeMillis() - data.lastActivityTime;
        return inactiveTime > ModConfig.getInstance().chunkColdTimeout * 1000;
    }

    private void cleanupInactiveChunks(long currentTime) {
        chunkActivityMap.entrySet().removeIf(entry -> {
            long inactiveTime = currentTime - entry.getValue().lastActivityTime;
            return inactiveTime > ModConfig.getInstance().chunkColdTimeout * 1000;
        });
    }

    private static class ChunkActivityData {
        long lastActivityTime;

        ChunkActivityData(long lastActivityTime) {
            this.lastActivityTime = lastActivityTime;
        }
    }
} 