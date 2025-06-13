package net.tockmod;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.tockmod.command.TockCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TockServerMod implements DedicatedServerModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tock/Server");

    @Override
    public void onInitializeServer() {
        LOGGER.info("Initializing Tock Server Components");

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TockCommands.register(dispatcher);
        });

        LOGGER.info("Tock Server initialization complete!");
    }
} 