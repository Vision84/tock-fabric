package net.tockmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.tockmod.config.ModConfig;
import net.tockmod.tick.NeuroTickController;
import net.tockmod.chunk.ChunkFuseManager;
import net.tockmod.entity.SnailSpawnManager;
import net.tockmod.scheduler.SmartScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TockMod implements ModInitializer {
    public static final String MOD_ID = "tock";
    public static final String MOD_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static TockMod instance;
    private MinecraftServer server;
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
        LOGGER.info("Running Tock {} on Java {}", MOD_VERSION, System.getProperty("java.version"));
        
        // Load configuration
        ModConfig.load();
        LOGGER.info("Configuration loaded: neurotick={}, chunkfuse={}, snailspawn={}, scheduler={}",
            ModConfig.getInstance().neurotickEnabled,
            ModConfig.getInstance().chunkfuseEnabled,
            ModConfig.getInstance().snailspawnEnabled,
            ModConfig.getInstance().schedulerEnabled);
        
        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            LOGGER.info("Server started - Tock is now active");
            LOGGER.info("Server details: name={}, version={}, player count={}",
                server.getName(),
                server.getVersion(),
                server.getCurrentPlayerCount());
            
            // Log world information
            server.getWorlds().forEach(world -> {
                LOGGER.info("World loaded: {} (dimension: {})",
                    world.getRegistryKey().getValue(),
                    world.getDimension().toString());
            });
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            LOGGER.info("Server stopping - Tock is now inactive");
            this.server = null;
        });
        
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

    public MinecraftServer getServer() {
        if (server == null) {
            LOGGER.error("Server instance is null at this lifecycle point");
            LOGGER.error("Stack trace:", new RuntimeException("Server access stack trace"));
        }
        return server;
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