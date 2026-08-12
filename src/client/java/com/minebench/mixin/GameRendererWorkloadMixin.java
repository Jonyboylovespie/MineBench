package com.minebench.mixin;

import com.minebench.client.RenderWorkloadController;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererWorkloadMixin {
    @Shadow @Final private GameRenderState gameRenderState;

    @Inject(method = "extractWindow", at = @At("TAIL"))
    private void minebench$applyInternalRenderScale(CallbackInfo callbackInfo) {
        RenderWorkloadController.apply(gameRenderState.windowRenderState);
    }
}
