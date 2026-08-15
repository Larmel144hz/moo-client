package com.mooclient.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to change the Minecraft window title to "Moo Client 1.21.4"
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void mooClient$getWindowTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("Moo Client 1.21.4");
    }
}
