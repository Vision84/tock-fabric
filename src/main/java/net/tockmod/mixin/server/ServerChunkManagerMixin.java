package net.tockmod.mixin.server;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.tockmod.TockMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Inject(method = "tickChunks", at = @At("HEAD"))
    private void onTickChunksStart(CallbackInfo ci) {
        ServerChunkManager manager = (ServerChunkManager) (Object) this;
        ServerWorld world = manager.getWorld();
        TockMod.getInstance().getChunkFuseManager().onServerTickStart(world.getServer());
    }

    @Inject(method = "tickChunks", at = @At("RETURN"))
    private void onTickChunksEnd(CallbackInfo ci) {
        ServerChunkManager manager = (ServerChunkManager) (Object) this;
        ServerWorld world = manager.getWorld();
        TockMod.getInstance().getChunkFuseManager().onServerTickEnd(world.getServer());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        ServerChunkManager manager = (ServerChunkManager) (Object) this;
        ServerWorld world = manager.getWorld();
        ChunkPos pos = new ChunkPos(world.getSpawnPos());
        TockMod.getInstance().getChunkFuseManager().markChunkActive(world, pos);
    }
} 