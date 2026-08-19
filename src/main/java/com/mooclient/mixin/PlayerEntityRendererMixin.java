package com.mooclient.mixin;

import com.mooclient.module.modules.NametagsModule;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @ModifyVariable(
        method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Text mooClient$modifyNametagText(Text text, PlayerEntityRenderState state) {
        if (NametagsModule.isNametagsEnabled() && state != null && text != null) {
            // Do NOT format if text is the scoreboard sub-label (e.g. "20 ❤")
            if (state.playerName != null && text == state.playerName) {
                return text;
            }
            if (state.name != null && !text.getString().toLowerCase().contains(state.name.toLowerCase())) {
                return text;
            }
            return NametagsModule.formatNametag(text, state.id, state.name);
        }
        return text;
    }
}
