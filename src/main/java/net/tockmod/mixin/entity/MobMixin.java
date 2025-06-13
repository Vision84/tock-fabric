package net.tockmod.mixin.entity;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.tockmod.TockMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTickStart(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        
        // Skip ticking if the chunk is cold
        if (mob.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) mob.getWorld();
            ChunkPos chunkPos = new ChunkPos(mob.getBlockPos());
            
            if (TockMod.getInstance().getChunkFuseManager().isChunkCold(chunkPos)) {
                ci.cancel();
            }
        }
    }
} 