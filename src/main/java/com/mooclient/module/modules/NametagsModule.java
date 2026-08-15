package com.mooclient.module.modules;

import com.mooclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Nametags Module.
 * Always shows own nametag in 3rd person / Freelook.
 * Displays colorful latency (ping) indicators above player heads.
 * Displays authentic Lunar/Badlion style Moo Client logo badge before nicknames (always active).
 * Option to remove background behind nametags.
 * Option to enable text shadow.
 */
public class NametagsModule extends Module {

    public enum PingPosition {
        BESIDE("Obok / Beside"),
        ABOVE("Nad / Above");

        private final String displayName;

        PingPosition(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private static boolean enabled = true;
    private static boolean showLogo = true;
    private static boolean showPing = true;
    private static PingPosition pingPosition = PingPosition.BESIDE;
    private static boolean removeBackground = false;
    private static boolean textShadow = true;

    public NametagsModule() {
        super("Nametags", "Wyświetla nicki, logo i kolorowy ping nad graczami", Category.RENDER);
        setEnabled(true);
    }

    @Override
    public void onEnable() {
        enabled = true;
    }

    @Override
    public void onDisable() {
        enabled = false;
    }

    public static boolean isNametagsEnabled() {
        return enabled;
    }

    public static void setNametagsEnabled(boolean state) {
        enabled = state;
        com.mooclient.module.ModuleManager.getInstance().getModule("Nametags").ifPresent(m -> {
            if (m.isEnabled() != state) {
                m.setEnabled(state);
            }
        });
    }

    public static boolean isShowLogo() {
        return showLogo;
    }

    public static void setShowLogo(boolean state) {
        showLogo = state;
    }

    public static void toggleShowLogo() {
        showLogo = !showLogo;
    }

    public static boolean isShowPing() {
        return showPing;
    }

    public static void setShowPing(boolean state) {
        showPing = state;
    }

    public static void toggleShowPing() {
        showPing = !showPing;
    }

    public static PingPosition getPingPosition() {
        return pingPosition;
    }

    public static void setPingPosition(PingPosition pos) {
        pingPosition = pos;
    }

    public static void cyclePingPosition() {
        pingPosition = (pingPosition == PingPosition.BESIDE) ? PingPosition.ABOVE : PingPosition.BESIDE;
    }

    public static boolean isRemoveBackground() {
        return removeBackground;
    }

    public static void setRemoveBackground(boolean state) {
        removeBackground = state;
    }

    public static void toggleRemoveBackground() {
        removeBackground = !removeBackground;
    }

    public static boolean isTextShadow() {
        return textShadow;
    }

    public static void setTextShadow(boolean state) {
        textShadow = state;
    }

    public static void toggleTextShadow() {
        textShadow = !textShadow;
    }

    /**
     * Retrieves colored latency Text indicator for the given entity ID.
     */
    public static Text getPingText(int entityId) {
        if (!enabled || !showPing) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.getNetworkHandler() == null) {
            return null;
        }

        if (!(client.world.getEntityById(entityId) instanceof PlayerEntity player)) {
            return null;
        }

        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (entry == null) {
            return null;
        }

        int ping = entry.getLatency();
        Formatting pingColor;
        if (ping <= 50) {
            pingColor = Formatting.GREEN;      // §a (0-50ms)
        } else if (ping <= 100) {
            pingColor = Formatting.DARK_GREEN; // §2 (51-100ms)
        } else if (ping <= 150) {
            pingColor = Formatting.YELLOW;     // §e (101-150ms)
        } else if (ping <= 250) {
            pingColor = Formatting.GOLD;       // §6 (151-250ms)
        } else {
            pingColor = Formatting.RED;        // §c (250ms+)
        }

        return Text.literal("[" + ping + "ms]").formatted(pingColor);
    }

    /**
     * Formats player nametag with colorful latency indicator.
     */
    public static Text formatNametag(Text originalText, int entityId) {
        if (!enabled || originalText == null) {
            return originalText;
        }

        if (showPing && pingPosition == PingPosition.BESIDE) {
            Text pingText = getPingText(entityId);
            if (pingText != null) {
                return originalText.copy().append(Text.literal(" ")).append(pingText);
            }
        }

        return originalText;
    }
}
