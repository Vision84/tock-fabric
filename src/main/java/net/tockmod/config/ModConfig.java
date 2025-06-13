package net.tockmod.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.tockmod.TockMod;

@Config(name = TockMod.MOD_ID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("neurotick")
    @Comment("Enable the NeuroTick system for tick budget management")
    public boolean neurotickEnabled = true;

    @ConfigEntry.Category("neurotick")
    @Comment("Maximum milliseconds per tick (default: 40ms for 20 TPS)")
    public int maxTickTime = 40;

    @ConfigEntry.Category("chunkfuse")
    @Comment("Enable the ChunkFuse system for chunk activity tracking")
    public boolean chunkfuseEnabled = true;

    @ConfigEntry.Category("chunkfuse")
    @Comment("Seconds of inactivity before a chunk is considered 'cold'")
    public int chunkColdTimeout = 30;

    @ConfigEntry.Category("snailspawn")
    @Comment("Enable the SnailSpawn system for controlled entity spawning")
    public boolean snailspawnEnabled = true;

    @ConfigEntry.Category("snailspawn")
    @Comment("Maximum entities that can spawn per tick")
    public int maxSpawnsPerTick = 10;

    @ConfigEntry.Category("scheduler")
    @Comment("Enable the Smart Scheduler system")
    public boolean schedulerEnabled = true;

    @ConfigEntry.Category("scheduler")
    @Comment("Enable preemptive cancellation of redundant updates")
    public boolean enablePreemptiveCancellation = true;

    private static ModConfig INSTANCE;

    public static void load() {
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
            me.shedaniel.autoconfig.AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
            INSTANCE = me.shedaniel.autoconfig.AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        }
    }

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }
} 