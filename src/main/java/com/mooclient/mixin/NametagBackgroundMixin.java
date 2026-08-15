package com.mooclient.mixin;

import com.mooclient.module.modules.NametagsModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into EntityRenderer to render authentic Lunar/Badlion style Moo Client logo badge
 * before player nicknames (always active for brand recognition), remove nametag background,
 * and apply text shadow.
 */
@Mixin(EntityRenderer.class)
public abstract class NametagBackgroundMixin {

    @Shadow
    public abstract TextRenderer getTextRenderer();

    private static final Identifier MOO_LOGO = Identifier.of("minecraft", "icons/icon_128x128.png");

    @Redirect(
        method = "renderLabelIfPresent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getTextBackgroundOpacity(F)F")
    )
    private float mooClient$removeNametagBackground(GameOptions options, float fallback) {
        if (NametagsModule.isNametagsEnabled() && NametagsModule.isRemoveBackground()) {
            return 0.0F;
        }
        return options.getTextBackgroundOpacity(fallback);
    }

    @ModifyArg(
        method = "renderLabelIfPresent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"),
        index = 4
    )
    private boolean mooClient$modifyNametagShadow(boolean originalShadow) {
        if (NametagsModule.isNametagsEnabled() && NametagsModule.isTextShadow()) {
            return true;
        }
        return originalShadow;
    }

    @Inject(
        method = "renderLabelIfPresent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", shift = At.Shift.AFTER)
    )
    private void mooClient$centerNametagWithBadge(EntityRenderState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (state instanceof PlayerEntityRenderState playerState && com.mooclient.util.MooUserManager.isMooUser(playerState.name, playerState.id)) {
            float iconSize = 9.0f;
            float iconOffset = (iconSize + 2.5f) / 2.0f;
            matrices.translate(iconOffset, 0.0f, 0.0f);
        }
    }

    @Inject(
        method = "renderLabelIfPresent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V")
    )
    private void mooClient$renderClientLogoBadge(EntityRenderState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (state instanceof PlayerEntityRenderState playerState && com.mooclient.util.MooUserManager.isMooUser(playerState.name, playerState.id)) {
            float textWidth = this.getTextRenderer().getWidth(text);
            float startX = -textWidth / 2.0f;

            float iconSize = 9.0f;
            float iconX = startX - iconSize - 2.5f;
            float iconY = -0.5f;

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            // 1. See-through pass for through-wall visibility
            VertexConsumer seeThrough = vertexConsumers.getBuffer(RenderLayer.getTextSeeThrough(MOO_LOGO));
            seeThrough.vertex(matrix, iconX, iconY, 0.0f).color(255, 255, 255, 128).texture(0.0f, 0.0f).light(light);
            seeThrough.vertex(matrix, iconX, iconY + iconSize, 0.0f).color(255, 255, 255, 128).texture(0.0f, 1.0f).light(light);
            seeThrough.vertex(matrix, iconX + iconSize, iconY + iconSize, 0.0f).color(255, 255, 255, 128).texture(1.0f, 1.0f).light(light);
            seeThrough.vertex(matrix, iconX + iconSize, iconY, 0.0f).color(255, 255, 255, 128).texture(1.0f, 0.0f).light(light);

            // 2. Normal textured pass for full opacity in direct line of sight
            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getText(MOO_LOGO));
            buffer.vertex(matrix, iconX, iconY, 0.0f).color(255, 255, 255, 255).texture(0.0f, 0.0f).light(light);
            buffer.vertex(matrix, iconX, iconY + iconSize, 0.0f).color(255, 255, 255, 255).texture(0.0f, 1.0f).light(light);
            buffer.vertex(matrix, iconX + iconSize, iconY + iconSize, 0.0f).color(255, 255, 255, 255).texture(1.0f, 1.0f).light(light);
            buffer.vertex(matrix, iconX + iconSize, iconY, 0.0f).color(255, 255, 255, 255).texture(1.0f, 0.0f).light(light);
        }
    }
}
