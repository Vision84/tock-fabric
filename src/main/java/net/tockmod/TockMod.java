package net.tockmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.tockmod.config.ModConfig;
import net.tockmod.tick.NeuroTickController;
import net.tockmod.chunk.ChunkFuseManager;
import net.tockmod.entity.SnailSpawnManager;
import net.tockmod.scheduler.SmartScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TockMod implements ModInitializer {
    public static final String MOD_ID = "tock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static TockMod instance;
    private final NeuroTickController neuroTickController;
    private final ChunkFuseManager chunkFuseManager;
    private final SnailSpawnManager snailSpawnManager;
    private final SmartScheduler smartScheduler;

    public TockMod() {
        instance = this;
        
        // Initialize core systems
        this.neuroTickController = new NeuroTickController();
        this.chunkFuseManager = new ChunkFuseManager();
        this.snailSpawnManager = new SnailSpawnManager();
        this.smartScheduler = new SmartScheduler();
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Tock - The Tick-Aware Orchestrator of Chunk Kinetics");
        
        // Load configuration
        ModConfig.load();
        
        // Register tick events
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            neuroTickController.onServerTickStart(server);
            chunkFuseManager.onServerTickStart(server);
            snailSpawnManager.onServerTickStart(server);
            smartScheduler.onServerTickStart(server);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            neuroTickController.onServerTickEnd(server);
            chunkFuseManager.onServerTickEnd(server);
            snailSpawnManager.onServerTickEnd(server);
            smartScheduler.onServerTickEnd(server);
        });

        LOGGER.info("Tock initialization complete!");
    }

    public static TockMod getInstance() {
        return instance;
    }

    public NeuroTickController getNeuroTickController() {
        return neuroTickController;
    }

    public ChunkFuseManager getChunkFuseManager() {
        return chunkFuseManager;
    }

    public SnailSpawnManager getSnailSpawnManager() {
        return snailSpawnManager;
    }

    public SmartScheduler getSmartScheduler() {
        return smartScheduler;
    }
} 