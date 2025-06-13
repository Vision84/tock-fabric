package net.tockmod.mixin.server;

import net.minecraft.server.world.ServerWorld;
import net.tockmod.TockMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        TockMod.getInstance().getNeuroTickController().onServerTickStart(world.getServer());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        TockMod.getInstance().getNeuroTickController().onServerTickEnd(world.getServer());
    }
} 