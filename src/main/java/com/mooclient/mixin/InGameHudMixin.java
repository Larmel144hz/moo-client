package com.mooclient.mixin;

import com.mooclient.module.modules.FpsModule;
import com.mooclient.module.modules.PotionEffectsModule;
import com.mooclient.module.modules.ToggleSprintModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

/**
 * Mixin to render in-game HUD modules (FPS, Sprint, Potion Effects) at customizable positions.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderMainHud", at = @At("TAIL"))
    private void mooClient$renderHudElements(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) {
            return;
        }

        // 1. FPS Module Rendering
        if (FpsModule.isFpsEnabled()) {
            int fps = client.getCurrentFps();
            FpsModule.FpsStyle style = FpsModule.getStyle();

            String fpsText;
            if (style == FpsModule.FpsStyle.BRACKETS) {
                fpsText = "[" + fps + " FPS]";
            } else if (FpsModule.isShowPrefix()) {
                fpsText = "FPS: " + fps;
            } else {
                fpsText = fps + " FPS";
            }

            int textWidth = client.textRenderer.getWidth(fpsText);
            FpsModule.width = textWidth + 6;
            FpsModule.height = 12;

            int x = FpsModule.posX;
            int y = FpsModule.posY;

            if (style == FpsModule.FpsStyle.MOO_CLIENT) {
                context.fill(x - 2, y - 2, x + textWidth + 4, y + 10, 0x88000000);
                context.fill(x - 3, y - 2, x - 2, y + 10, 0xFFFFFFFF);
                context.drawText(client.textRenderer, fpsText, x + 2, y, 0xFFFFFFFF, true);
            } else {
                if (FpsModule.isShowBackground()) {
                    context.fill(x - 2, y - 2, x + textWidth + 2, y + 10, 0x66000000);
                }
                context.drawText(client.textRenderer, fpsText, x, y, 0xFFFFFFFF, FpsModule.isTextShadow());
            }
        }

        // 2. Sprint Module Rendering
        if (ToggleSprintModule.isSprintEnabled() && (ToggleSprintModule.shouldSprint() || client.currentScreen instanceof com.mooclient.gui.MooClientScreen)) {
            ToggleSprintModule.SprintStyle style = ToggleSprintModule.getStyle();

            String sprintText;
            if (style == ToggleSprintModule.SprintStyle.BRACKETS) {
                sprintText = "[Sprinting]";
            } else if (style == ToggleSprintModule.SprintStyle.SIMPLE) {
                sprintText = "Sprinting";
            } else {
                sprintText = "Sprinting (Toggled)";
            }

            int textWidth = client.textRenderer.getWidth(sprintText);
            ToggleSprintModule.width = textWidth + 6;
            ToggleSprintModule.height = 12;

            int x = ToggleSprintModule.posX;
            int y = ToggleSprintModule.posY;

            if (style == ToggleSprintModule.SprintStyle.MOO_CLIENT) {
                context.fill(x - 2, y - 2, x + textWidth + 4, y + 10, 0x88000000);
                context.fill(x - 3, y - 2, x - 2, y + 10, 0xFFFFFFFF);
                context.drawText(client.textRenderer, sprintText, x + 2, y, 0xFFFFFFFF, true);
            } else {
                if (ToggleSprintModule.isShowBackground()) {
                    context.fill(x - 2, y - 2, x + textWidth + 2, y + 10, 0x66000000);
                }
                context.drawText(client.textRenderer, sprintText, x, y, 0xFFFFFFFF, ToggleSprintModule.isTextShadow());
            }
        }

        // 3. Potion Effects HUD Rendering (Moo Client / Simple / Compact)
        if (PotionEffectsModule.isModuleEnabled() && client.player != null) {
            Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
            boolean isMenu = client.currentScreen instanceof com.mooclient.gui.MooClientScreen;

            if (!effects.isEmpty() || isMenu) {
                int startX = PotionEffectsModule.posX;
                int curY = PotionEffectsModule.posY;
                PotionEffectsModule.PotionStyle pStyle = PotionEffectsModule.getStyle();
                boolean bg = PotionEffectsModule.isShowBackground();
                boolean shadow = PotionEffectsModule.isTextShadow();
                boolean showIcon = PotionEffectsModule.isShowIcon();

                int maxW = 100;
                int totalH = 0;
                int rowH = (pStyle == PotionEffectsModule.PotionStyle.COMPACT) ? 14 : 22;
                int rowGap = (pStyle == PotionEffectsModule.PotionStyle.COMPACT) ? 3 : 4;

                if (effects.isEmpty() && isMenu) {
                    // Preview dummy effects in MooClientScreen menu
                    RegistryEntry<StatusEffect>[] sampleEffects = new RegistryEntry[]{
                        StatusEffects.SPEED,
                        StatusEffects.POISON,
                        StatusEffects.FIRE_RESISTANCE
                    };
                    String[] sampleNames = new String[]{"Speed", "Poison", "Fire Resistance"};
                    String[] sampleTimes = new String[]{"5:11", "0:25", "5:10"};
                    int[] sampleColors = new int[]{0xFF7CAFC6, 0xFF4E9331, 0xFFE49A3A};

                    for (int i = 0; i < sampleNames.length; i++) {
                        String name = sampleNames[i];
                        String time = sampleTimes[i];
                        RegistryEntry<StatusEffect> effectEntry = sampleEffects[i];
                        int effectColor = sampleColors[i];

                        int itemW;
                        if (pStyle == PotionEffectsModule.PotionStyle.COMPACT) {
                            String compactLine = name + " §7" + time;
                            itemW = (showIcon ? 18 : 0) + client.textRenderer.getWidth(compactLine) + 8;
                        } else {
                            int nameW = client.textRenderer.getWidth(name);
                            int timeW = client.textRenderer.getWidth(time);
                            itemW = (showIcon ? 22 : 0) + Math.max(nameW, timeW) + (pStyle == PotionEffectsModule.PotionStyle.MOO_CLIENT ? 12 : 6);
                        }
                        maxW = Math.max(maxW, itemW);

                        if (pStyle == PotionEffectsModule.PotionStyle.MOO_CLIENT) {
                            context.fill(startX - 2, curY - 2, startX + itemW, curY + rowH, 0x77000000);
                            context.fill(startX - 2, curY - 2, startX, curY + rowH, effectColor); // Colored accent bar on left
                        } else if (bg) {
                            context.fill(startX - 2, curY - 2, startX + itemW, curY + rowH, 0x66000000);
                        }

                        // 1. Draw Potion Icon
                        int textX = startX + (pStyle == PotionEffectsModule.PotionStyle.MOO_CLIENT ? 4 : 0);
                        if (showIcon) {
                            try {
                                Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effectEntry);
                                if (sprite != null) {
                                    if (pStyle == PotionEffectsModule.PotionStyle.COMPACT) {
                                        context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, textX, curY + 1, 12, 12);
                                    } else {
                                        context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, textX, curY + 1, 18, 18);
                                    }
                                }
                            } catch (Exception ignored) {}
                            textX += (pStyle == PotionEffectsModule.PotionStyle.COMPACT) ? 16 : 22;
                        }

                        // 2. Draw Text
                        if (pStyle == PotionEffectsModule.PotionStyle.COMPACT) {
                            context.drawText(client.textRenderer, name + " §7" + time, textX, curY + 2, 0xFFFFFFFF, shadow);
                        } else if (pStyle == PotionEffectsModule.PotionStyle.MOO_CLIENT) {
                            context.drawText(client.textRenderer, name, textX, curY + 1, 0xFFFFFFFF, shadow);
                            context.drawText(client.textRenderer, "§7" + time, textX, curY + 10, 0xFFAAAAAA, shadow);
                        } else {
                            // SIMPLE
                            context.drawText(client.textRenderer, name, textX, curY + 1, 0xFFFFFFFF, shadow);
                            context.drawText(client.textRenderer, time, textX, curY + 10, 0xFFFFFFFF, shadow);
                        }

                        curY += rowH + rowGap;
                        totalH += rowH + rowGap;
                    }
                } else {
                    for (StatusEffectInstance effect : effects) {
                        RegistryEntry<StatusEffect> effectEntry = effect.getEffectType();
                        StatusEffect statusEffect = effectEntry.value();
                        String name = statusEffect.getName().getString() + PotionEffectsModule.getAmplifierString(effect.getAmplifier());
                        String duration = PotionEffectsModule.formatDuration(effect);
                        int color = 0xFF000000 | statusEffect.getColor();

                        int itemW;
                        if (pStyle == PotionEffectsModule.PotionStyle.COMPACT) {
                            String compactLine = name + " §7" + duration;
                            itemW = (showIcon ? 18 : 0) + client.textRenderer.getWidth(compactLine) + 8;
                        } else {
                            int nameW = client.textRenderer.getWidth(name);
                            int timeW = client.textRenderer.getWidth(duration);
                            itemW = (showIcon ? 22 : 0) + Math.max(nameW, timeW) + (pStyle == PotionEffectsModule.PotionStyle.MOO_CLIENT ? 12 : 6);
                        }
                        maxW = Math.max(maxW, itemW);

                        if (pStyle == PotionEffectsModule.PotionStyle.MOO_CLIENT) {
                            context.fill(startX - 2, curY - 2, startX + itemW, curY + rowH, 0x77000000);
                            context.fill(startX - 2, curY - 2, startX, curY + rowH, color); // Colored accent bar on left
                        } else if (bg) {
                            context.fill(startX - 2, curY - 2, startX + itemW, curY + rowH, 0x66000000);
                        }

                        // 1. Draw Potion Icon
                        int textX = startX + (pStyle == PotionEffectsModule.PotionStyle.MOO_CLIENT ? 4 : 0);
                        if (showIcon) {
                            try {
                                Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effectEntry);
                                if (sprite != null) {
                                    if (pStyle == PotionEffectsModule.PotionStyle.COMPACT) {
                                        context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, textX, curY + 1, 12, 12);
                                    } else {
                                        context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, textX, curY + 1, 18, 18);
                                    }
                                }
                            } catch (Exception ignored) {}
                            textX += (pStyle == PotionEffectsModule.PotionStyle.COMPACT) ? 16 : 22;
                        }

                        // 2. Draw Text
                        if (pStyle == PotionEffectsModule.PotionStyle.COMPACT) {
                            context.drawText(client.textRenderer, name + " §7" + duration, textX, curY + 2, 0xFFFFFFFF, shadow);
                        } else if (pStyle == PotionEffectsModule.PotionStyle.MOO_CLIENT) {
                            context.drawText(client.textRenderer, name, textX, curY + 1, 0xFFFFFFFF, shadow);
                            context.drawText(client.textRenderer, "§7" + duration, textX, curY + 10, 0xFFAAAAAA, shadow);
                        } else {
                            // SIMPLE
                            context.drawText(client.textRenderer, name, textX, curY + 1, 0xFFFFFFFF, shadow);
                            context.drawText(client.textRenderer, duration, textX, curY + 10, 0xFFFFFFFF, shadow);
                        }

                        curY += rowH + rowGap;
                        totalH += rowH + rowGap;
                    }
                }

                PotionEffectsModule.width = maxW + 4;
                PotionEffectsModule.height = Math.max(26, totalH);
            }
        }
    }
}
