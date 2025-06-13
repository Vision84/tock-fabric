package net.tockmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.server.world.ServerWorld;
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
        LOGGER.debug("Reset spawn counter for new tick");

        // Log spawn summary every 100 ticks
        if (server.getTicks() % 100 == 0) {
            logSpawnSummary();
        }
    }

    private void logSpawnSummary() {
        LOGGER.info("=== Spawn Summary ===");
        LOGGER.info("Total spawn data entries: {}", spawnDataMap.size());
        
        // Group spawns by world
        Map<ServerWorld, Long> spawnsByWorld = spawnDataMap.entrySet().stream()
            .collect(java.util.stream.Collectors.groupingBy(
                entry -> entry.getValue().world,
                java.util.stream.Collectors.counting()));
        
        spawnsByWorld.forEach((world, count) -> 
            LOGGER.info("  {}: {} spawn points", world.getRegistryKey().getValue(), count));
        
        // Count active vs inactive spawn points
        long activeSpawns = spawnDataMap.values().stream()
            .filter(data -> data.isActive())
            .count();
        
        LOGGER.info("Active spawn points: {}", activeSpawns);
        LOGGER.info("Inactive spawn points: {}", spawnDataMap.size() - activeSpawns);
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
            LOGGER.debug("Spawn limit reached for this tick ({} entities)", ModConfig.getInstance().maxSpawnsPerTick);
            return false;
        }

        // Get or create spawn data for this chunk
        SpawnData data = spawnDataMap.computeIfAbsent(pos, k -> {
            LOGGER.debug("Created new spawn data for chunk {}", pos);
            return new SpawnData();
        });

        // Check entity type specific limits
        if (!data.canSpawnEntityType(entityType)) {
            LOGGER.debug("Entity type limit reached for {} in chunk {}", entityType, pos);
            return false;
        }

        // Increment spawn counters
        spawnsThisTick.incrementAndGet();
        data.recordSpawn(entityType);
        LOGGER.debug("Spawned {} in chunk {} (total this tick: {})", 
            entityType, 
            pos, 
            spawnsThisTick.get());
        return true;
    }

    public void recordSpawnAttempt(ServerWorld world, ChunkPos pos, boolean success) {
        if (!ModConfig.getInstance().snailspawnEnabled) {
            return;
        }

        SpawnData data = spawnDataMap.computeIfAbsent(pos, 
            k -> new SpawnData(world));
        
        data.recordSpawnAttempt(success);
        LOGGER.debug("Recorded {} spawn attempt at {} in {}", 
            success ? "successful" : "failed",
            pos,
            world.getRegistryKey().getValue());
    }

    private static class SpawnData {
        private final Map<EntityType<?>, Integer> entityTypeCounts = new ConcurrentHashMap<>();
        private static final int MAX_ENTITIES_PER_TYPE = 10;
        private final ServerWorld world;
        private int totalAttempts;
        private int successfulAttempts;
        private long lastAttemptTime;

        public SpawnData() {
            this.world = null;
            this.totalAttempts = 0;
            this.successfulAttempts = 0;
            this.lastAttemptTime = System.currentTimeMillis();
        }

        public SpawnData(ServerWorld world) {
            this.world = world;
            this.totalAttempts = 0;
            this.successfulAttempts = 0;
            this.lastAttemptTime = System.currentTimeMillis();
        }

        public boolean canSpawnEntityType(EntityType<?> type) {
            return entityTypeCounts.getOrDefault(type, 0) < MAX_ENTITIES_PER_TYPE;
        }

        public void recordSpawn(EntityType<?> type) {
            entityTypeCounts.merge(type, 1, Integer::sum);
        }

        public void recordSpawnAttempt(boolean success) {
            this.totalAttempts++;
            if (success) {
                this.successfulAttempts++;
            }
            this.lastAttemptTime = System.currentTimeMillis();
        }

        public boolean isActive() {
            return System.currentTimeMillis() - lastAttemptTime < 
                ModConfig.getInstance().chunkColdTimeout * 1000;
        }

        public double getSuccessRate() {
            return totalAttempts > 0 ? (double) successfulAttempts / totalAttempts : 0;
        }
    }
} 