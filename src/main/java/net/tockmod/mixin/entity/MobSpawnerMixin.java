package net.tockmod.mixin.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.tockmod.TockMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobSpawnerBlockEntity.class)
public class MobSpawnerMixin {
    @Inject(method = "spawnEntity(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/SpawnReason;)Z", at = @At("HEAD"), cancellable = true)
    private void onSpawnEntity(ServerWorld world, BlockPos pos, EntityType<?> entityType, SpawnReason reason, CallbackInfoReturnable<Boolean> cir) {
        ChunkPos chunkPos = new ChunkPos(pos);
        
        // Check if we can spawn this entity
        if (!TockMod.getInstance().getSnailSpawnManager().canSpawnEntity(chunkPos, entityType)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
} 