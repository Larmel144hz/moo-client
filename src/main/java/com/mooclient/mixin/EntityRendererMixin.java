package com.mooclient.mixin;

import com.mooclient.module.modules.NametagsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into LivingEntityRenderer.hasLabel to force showing own nametag in 3rd person.
 * Must target LivingEntityRenderer because it overrides EntityRenderer.hasLabel.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void mooClient$showOwnNametag(LivingEntity entity, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
        if (NametagsModule.isNametagsEnabled() && entity instanceof AbstractClientPlayerEntity player) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (player == client.player && !client.options.getPerspective().isFirstPerson()) {
                cir.setReturnValue(true);
            }
        }
    }
}
