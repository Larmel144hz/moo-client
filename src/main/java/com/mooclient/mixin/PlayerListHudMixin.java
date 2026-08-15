package com.mooclient.mixin;

import com.mooclient.module.modules.NametagsModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Renders the Moo Client cow logo badge right before player nicknames on the Tab list.
 * Output: [Head] [Cow Logo] Nickname
 */
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    private static final Identifier MOO_LOGO = Identifier.of("mooclient", "textures/gui/icon.png");

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"
        )
    )
    private int mooClient$renderPlayerNameWithLogo(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color) {
        if (text != null && NametagsModule.isNametagsEnabled() && NametagsModule.isShowLogo()) {
            // Draw 8x8 cow logo badge before every player nickname
            context.drawTexture(RenderLayer::getGuiTextured, MOO_LOGO, x, y, 0.0f, 0.0f, 8, 8, 8, 8);
            // Draw player nickname shifted right by 10px
            return context.drawTextWithShadow(textRenderer, text, x + 10, y, color);
        }
        return context.drawTextWithShadow(textRenderer, text, x, y, color);
    }
}
