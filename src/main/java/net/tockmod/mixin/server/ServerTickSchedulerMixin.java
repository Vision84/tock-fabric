package net.tockmod.mixin.server;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.util.math.BlockPos;
import net.tockmod.TockMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerTickScheduler.class)
public class ServerTickSchedulerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        ServerTickScheduler<?> scheduler = (ServerTickScheduler<?>) (Object) this;
        ServerWorld world = scheduler.getWorld();
        TockMod.getInstance().getSmartScheduler().onServerTickStart(world.getServer());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        ServerTickScheduler<?> scheduler = (ServerTickScheduler<?>) (Object) this;
        ServerWorld world = scheduler.getWorld();
        TockMod.getInstance().getSmartScheduler().onServerTickEnd(world.getServer());
    }

    @Inject(method = "schedule", at = @At("HEAD"), cancellable = true)
    private void onSchedule(BlockPos pos, int priority, CallbackInfo ci) {
        ServerTickScheduler<?> scheduler = (ServerTickScheduler<?>) (Object) this;
        ServerWorld world = scheduler.getWorld();
        
        // Use our smart scheduler instead
        TockMod.getInstance().getSmartScheduler().scheduleTask(world, pos, () -> {
            // Original tick logic would go here
        }, priority);
        
        ci.cancel(); // Prevent the original scheduling
    }
} 