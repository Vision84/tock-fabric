package net.tockmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.tockmod.TockMod;
import net.tockmod.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TockCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tock/Commands");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tock")
            .then(CommandManager.literal("debug")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    TockMod mod = TockMod.getInstance();
                    
                    source.sendMessage(Text.literal("=== Tock Debug Information ==="));
                    source.sendMessage(Text.literal(String.format("Last Tick Time: %dms", mod.getNeuroTickController().getLastTickTime())));
                    source.sendMessage(Text.literal(String.format("Average Tick Time: %.2fms", mod.getNeuroTickController().getAverageTickTime())));
                    source.sendMessage(Text.literal(String.format("Over Budget: %b", mod.getNeuroTickController().isOverBudget())));
                    
                    // Add configuration status
                    source.sendMessage(Text.literal("\n=== Configuration Status ==="));
                    source.sendMessage(Text.literal(String.format("NeuroTick Enabled: %b", ModConfig.getInstance().neurotickEnabled)));
                    source.sendMessage(Text.literal(String.format("ChunkFuse Enabled: %b", ModConfig.getInstance().chunkfuseEnabled)));
                    source.sendMessage(Text.literal(String.format("SnailSpawn Enabled: %b", ModConfig.getInstance().snailspawnEnabled)));
                    source.sendMessage(Text.literal(String.format("Smart Scheduler Enabled: %b", ModConfig.getInstance().schedulerEnabled)));
                    
                    // Add performance metrics
                    source.sendMessage(Text.literal("\n=== Performance Metrics ==="));
                    source.sendMessage(Text.literal(String.format("Max Tick Time: %dms", ModConfig.getInstance().maxTickTime)));
                    source.sendMessage(Text.literal(String.format("Chunk Cold Timeout: %ds", ModConfig.getInstance().chunkColdTimeout)));
                    source.sendMessage(Text.literal(String.format("Max Spawns Per Tick: %d", ModConfig.getInstance().maxSpawnsPerTick)));
                    
                    return 1;
                }))
            .then(CommandManager.literal("profile")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    source.sendMessage(Text.literal("=== Tock Profiling Information ==="));
                    source.sendMessage(Text.literal("Profiling not implemented yet"));
                    return 1;
                }))
            .then(CommandManager.literal("heatmap")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    source.sendMessage(Text.literal("=== Tock Chunk Heatmap ==="));
                    source.sendMessage(Text.literal("Heatmap not implemented yet"));
                    return 1;
                }))
        );
    }
} 