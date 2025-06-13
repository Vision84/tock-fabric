package net.tockmod.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.tick.SimpleTickScheduler;
import net.minecraft.util.math.BlockPos;
import net.tockmod.TockMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleTickScheduler.class)
public class ServerTickSchedulerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("Tock/Mixin/TickScheduler");
    private static int tickCount = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        LOGGER.debug("Inject: onTickStart triggered for {}", this.getClass().getSimpleName());
        MinecraftServer server = TockMod.getInstance().getServer();
        if (server == null) {
            LOGGER.error("Failed to retrieve MinecraftServer in onTickStart");
            return;
        }
        TockMod.getInstance().getSmartScheduler().onServerTickStart(server);
        
        // Log every 100 ticks
        if (++tickCount % 100 == 0) {
            LOGGER.debug("100 ticks passed in TickScheduler");
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        LOGGER.debug("Inject: onTickEnd triggered for {}", this.getClass().getSimpleName());
        MinecraftServer server = TockMod.getInstance().getServer();
        if (server == null) {
            LOGGER.error("Failed to retrieve MinecraftServer in onTickEnd");
            return;
        }
        TockMod.getInstance().getSmartScheduler().onServerTickEnd(server);
    }

    @Inject(method = "scheduleTick", at = @At("HEAD"), cancellable = true)
    private void onScheduleTick(BlockPos pos, Object object, int delay, CallbackInfo ci) {
        LOGGER.debug("Inject: onScheduleTick triggered for {} at {}", this.getClass().getSimpleName(), pos);
        MinecraftServer server = TockMod.getInstance().getServer();
        if (server == null) {
            LOGGER.error("Failed to retrieve MinecraftServer in onScheduleTick");
            return;
        }

        // Get the overworld as our default world for scheduling
        ServerWorld world = server.getOverworld();
        if (world == null) {
            LOGGER.error("Failed to retrieve overworld in onScheduleTick");
            return;
        }

        // Use our smart scheduler instead
        TockMod.getInstance().getSmartScheduler().scheduleTask(world, pos, () -> {
            try {
                LOGGER.debug("Executing scheduled task at {}", pos);
                // Original tick logic would go here
            } catch (Exception e) {
                LOGGER.error("Task at {} failed: {}", pos, e.getMessage(), e);
            }
        }, delay);
        
        LOGGER.debug("Cancelling original scheduling for task at {}", pos);
        ci.cancel(); // Prevent the original scheduling
    }
} 