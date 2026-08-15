package com.mooclient.mixin;

import com.mooclient.gui.MooMainMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to redirect the default Minecraft TitleScreen to the custom MooMainMenuScreen.
 */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void mooClient$openCustomMainMenu(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && (client.currentScreen == null || client.currentScreen instanceof TitleScreen)) {
            client.setScreen(new MooMainMenuScreen());
            ci.cancel();
        }
    }
}
