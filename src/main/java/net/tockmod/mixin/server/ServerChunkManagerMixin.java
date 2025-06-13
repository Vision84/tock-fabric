package net.tockmod.mixin.server;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.tockmod.TockMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tock/Mixin/ChunkManager");

    @Inject(method = "tickChunks", at = @At("HEAD"))
    private void onTickChunksStart(CallbackInfo ci) {
        LOGGER.debug("Inject: onTickChunksStart triggered for {}", this.getClass().getSimpleName());
        ServerChunkManager manager = (ServerChunkManager) (Object) this;
        ServerWorld world = (ServerWorld) manager.getWorld();
        if (world == null) {
            LOGGER.error("Failed to retrieve ServerWorld in onTickChunksStart");
            return;
        }
        TockMod.getInstance().getChunkFuseManager().onServerTickStart(world.getServer());
    }

    @Inject(method = "tickChunks", at = @At("RETURN"))
    private void onTickChunksEnd(CallbackInfo ci) {
        LOGGER.debug("Inject: onTickChunksEnd triggered for {}", this.getClass().getSimpleName());
        ServerChunkManager manager = (ServerChunkManager) (Object) this;
        ServerWorld world = (ServerWorld) manager.getWorld();
        if (world == null) {
            LOGGER.error("Failed to retrieve ServerWorld in onTickChunksEnd");
            return;
        }
        TockMod.getInstance().getChunkFuseManager().onServerTickEnd(world.getServer());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        LOGGER.debug("Inject: onTickStart triggered for {}", this.getClass().getSimpleName());
        ServerChunkManager manager = (ServerChunkManager) (Object) this;
        ServerWorld world = (ServerWorld) manager.getWorld();
        if (world == null) {
            LOGGER.error("Failed to retrieve ServerWorld in onTickStart");
            return;
        }
        ChunkPos pos = new ChunkPos(world.getSpawnPos());
        TockMod.getInstance().getChunkFuseManager().markChunkActive(world, pos);
    }
} 