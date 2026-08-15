package com.mooclient.mixin;

import com.mooclient.module.modules.FullbrightModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into LightmapTextureManager to force maximum brightness
 * when the Gamma / Fullbright module is active.
 */
@Mixin(LightmapTextureManager.class)
public class FullbrightMixin {

    @Shadow private boolean dirty;

    /**
     * Ensures lightmap texture updates every frame while fullbright is active.
     */
    @Inject(method = "update", at = @At("HEAD"))
    private void mooClient$forceDirtyLightmap(float tickDelta, CallbackInfo ci) {
        if (FullbrightModule.isFullbrightActive()) {
            this.dirty = true;
        }
    }

    /**
     * Redirects Night Vision effect check in update() so shader NightVisionFactor is activated.
     */
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean mooClient$redirectNightVisionCheck(ClientPlayerEntity player, RegistryEntry<StatusEffect> effect) {
        if (FullbrightModule.isFullbrightActive() && StatusEffects.NIGHT_VISION.equals(effect)) {
            return true;
        }
        return player != null && player.hasStatusEffect(effect);
    }

    @Inject(method = "getBrightness(FI)F", at = @At("HEAD"), cancellable = true)
    private static void onGetBrightness(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (FullbrightModule.isFullbrightActive()) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F", at = @At("HEAD"), cancellable = true)
    private static void onGetDimensionBrightness(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (FullbrightModule.isFullbrightActive()) {
            cir.setReturnValue(1.0f);
        }
    }
}
