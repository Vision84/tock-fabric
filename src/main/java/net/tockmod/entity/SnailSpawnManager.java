package net.tockmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.tockmod.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SnailSpawnManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tock/SnailSpawn");
    private final Map<ChunkPos, SpawnData> spawnDataMap = new ConcurrentHashMap<>();
    private final AtomicInteger spawnsThisTick = new AtomicInteger(0);

    public void onServerTickStart(MinecraftServer server) {
        if (!ModConfig.getInstance().snailspawnEnabled) {
            return;
        }

        // Reset spawn counter for new tick
        spawnsThisTick.set(0);
    }

    public void onServerTickEnd(MinecraftServer server) {
        // No-op for now
    }

    public boolean canSpawnEntity(ChunkPos pos, EntityType<?> entityType) {
        if (!ModConfig.getInstance().snailspawnEnabled) {
            return true;
        }

        // Check global spawn limit
        if (spawnsThisTick.get() >= ModConfig.getInstance().maxSpawnsPerTick) {
            LOGGER.debug("Spawn limit reached for this tick");
            return false;
        }

        // Get or create spawn data for this chunk
        SpawnData data = spawnDataMap.computeIfAbsent(pos, k -> new SpawnData());

        // Check entity type specific limits
        if (!data.canSpawnEntityType(entityType)) {
            LOGGER.debug("Entity type limit reached for {} in chunk {}", entityType, pos);
            return false;
        }

        // Increment spawn counters
        spawnsThisTick.incrementAndGet();
        data.recordSpawn(entityType);
        return true;
    }

    private static class SpawnData {
        private final Map<EntityType<?>, Integer> entityTypeCounts = new ConcurrentHashMap<>();
        private static final int MAX_ENTITIES_PER_TYPE = 10;

        public boolean canSpawnEntityType(EntityType<?> type) {
            return entityTypeCounts.getOrDefault(type, 0) < MAX_ENTITIES_PER_TYPE;
        }

        public void recordSpawn(EntityType<?> type) {
            entityTypeCounts.merge(type, 1, Integer::sum);
        }
    }
} 