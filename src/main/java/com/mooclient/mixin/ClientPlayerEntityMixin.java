package com.mooclient.mixin;

import com.mooclient.module.modules.ToggleSprintModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into ClientPlayerEntity to implement Toggle Sprint.
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void mooClient$autoSprint(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (ToggleSprintModule.shouldSprint()
                && !player.isSneaking()
                && !player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !player.horizontalCollision
                && player.getHungerManager().getFoodLevel() > 6
                && player.input != null
                && player.input.movementForward > 0.1f) {
            player.setSprinting(true);
        }
    }
}
