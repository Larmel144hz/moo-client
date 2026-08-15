package com.mooclient.mixin;

import com.mooclient.gui.MooClientScreen;
import com.mooclient.module.modules.FullbrightModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables vanilla screen blur when MooClientScreen is open,
 * and handles Fullbright Night Vision strength calculations.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderBlur", at = @At("HEAD"), cancellable = true)
    private void mooClient$cancelBlur(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.currentScreen instanceof MooClientScreen) {
            ci.cancel();
        }
    }

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void mooClient$fullbrightNightVision(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (FullbrightModule.isFullbrightActive()) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void mooClient$applyZoomFov(net.minecraft.client.render.Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (com.mooclient.module.modules.ZoomModule.isZooming()) {
            float baseFov = cir.getReturnValueF();
            cir.setReturnValue(com.mooclient.module.modules.ZoomModule.calculateZoomFov(baseFov, tickDelta));
        }
    }
}
