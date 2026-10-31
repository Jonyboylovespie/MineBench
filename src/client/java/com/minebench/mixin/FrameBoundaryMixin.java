package com.minebench.mixin;

import com.minebench.client.MineBenchClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Version-pinned frame boundary for Minecraft 26.3. The method is public in the deobfuscated client API as
 * renderFrame(boolean), so exactly one callback is emitted per client frame. Menu/loading frames are gated in the
 * client adapter and never enter the sampler.
 */
@Mixin(Minecraft.class)
public final class FrameBoundaryMixin {
    @Inject(method = "renderFrame", at = @At("TAIL"))
    private void minebench$afterRenderFrame(boolean renderLevel, CallbackInfo callbackInfo) {
        MineBenchClient.onRenderedFrame();
    }
}
