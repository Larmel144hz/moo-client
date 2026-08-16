package com.mooclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks which players are confirmed Moo Client users.
 * The local player is always recognized as a Moo Client user.
 * Remote players are recognized in real-time when broadcasting on Moo Client.
 */
public class MooUserManager {

    private static final Set<String> MOO_USERS_NAMES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<UUID> MOO_USERS_UUIDS = Collections.synchronizedSet(new HashSet<>());
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,16}");

    public static void registerUser(String username, UUID uuid) {
        if (username != null && !username.trim().isEmpty()) {
            String clean = cleanName(username);
            if (!clean.isEmpty()) {
                MOO_USERS_NAMES.add(clean);
            }
        }
        if (uuid != null) {
            MOO_USERS_UUIDS.add(uuid);
        }
    }

    public static void unregisterUser(String username, UUID uuid) {
        if (username != null) {
            String clean = cleanName(username);
            if (!clean.isEmpty()) {
                MOO_USERS_NAMES.remove(clean);
            }
        }
        if (uuid != null) {
            MOO_USERS_UUIDS.remove(uuid);
        }
    }

    public static void clear() {
        MOO_USERS_NAMES.clear();
        MOO_USERS_UUIDS.clear();
    }

    public static String cleanName(String name) {
        if (name == null) return "";
        // Strip Minecraft color codes (§a, §f, etc.), wrapper strings, and trim
        return name.replaceAll("(?i)§[0-9a-fk-or]", "")
                   .replaceAll("(?i)literal\\{text='(.*?)'\\}", "$1")
                   .trim().toLowerCase();
    }

    /**
     * Checks if the player represented by the Tab list entry is a confirmed Moo Client user.
     */
    public static boolean isMooUser(PlayerListEntry entry) {
        if (entry == null || entry.getProfile() == null) return false;
        if (!com.mooclient.module.modules.NametagsModule.isNametagsEnabled() || !com.mooclient.module.modules.NametagsModule.isShowLogo()) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Always check local player
        if (client.getSession() != null && client.getSession().getUsername().equalsIgnoreCase(entry.getProfile().getName())) {
            return true;
        }
        if (client.player != null && client.player.getUuid() != null && client.player.getUuid().equals(entry.getProfile().getId())) {
            return true;
        }

        // 2. UUID Match
        if (entry.getProfile().getId() != null && MOO_USERS_UUIDS.contains(entry.getProfile().getId())) {
            return true;
        }

        // 3. Exact profile name match
        String nameClean = cleanName(entry.getProfile().getName());
        if (!nameClean.isEmpty() && MOO_USERS_NAMES.contains(nameClean)) {
            return true;
        }

        // 4. Match any words in display name
        if (entry.getDisplayName() != null) {
            String display = cleanName(entry.getDisplayName().getString());
            if (matchesAnyUser(display)) {
                return true;
            }
        }

        return matchesAnyUser(nameClean);
    }

    /**
     * Overload for Text object in nametag rendering
     */
    public static boolean isMooUser(Text text, int entityId) {
        if (text == null) return false;
        return isMooUser(text.getString(), entityId);
    }

    /**
     * Checks if the given player is a confirmed Moo Client user.
     */
    public static boolean isMooUser(String playerName, int entityId) {
        if (!com.mooclient.module.modules.NametagsModule.isNametagsEnabled() || !com.mooclient.module.modules.NametagsModule.isShowLogo()) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Local Player Check
        if (client.player != null && client.player.getId() == entityId) {
            return true;
        }
        if (client.getSession() != null) {
            String myClean = cleanName(client.getSession().getUsername());
            String targetClean = cleanName(playerName);
            if (!myClean.isEmpty() && (targetClean.equals(myClean) || targetClean.contains(myClean))) {
                return true;
            }
        }

        String targetClean = cleanName(playerName);
        if (targetClean.isEmpty()) return false;

        // 2. Direct username match in registered Moo users
        if (MOO_USERS_NAMES.contains(targetClean)) {
            return true;
        }

        // 3. Word token matcher (handles prefixes like "[VIP] Player", "Admin | Player", etc.)
        if (matchesAnyUser(targetClean)) {
            return true;
        }

        // 4. Check entity in world
        if (client.world != null && entityId >= 0) {
            Entity entity = client.world.getEntityById(entityId);
            if (entity instanceof PlayerEntity player) {
                if (player.getUuid() != null && MOO_USERS_UUIDS.contains(player.getUuid())) {
                    return true;
                }
                String entityClean = cleanName(player.getNameForScoreboard());
                if (!entityClean.isEmpty() && (MOO_USERS_NAMES.contains(entityClean) || matchesAnyUser(entityClean))) {
                    return true;
                }
            }
        }

        // 5. Tab list entry match
        if (client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                if (entry.getProfile() != null) {
                    if (MOO_USERS_UUIDS.contains(entry.getProfile().getId())) {
                        String profileClean = cleanName(entry.getProfile().getName());
                        if (targetClean.contains(profileClean) || profileClean.contains(targetClean)) {
                            return true;
                        }
                    }
                    String profileClean = cleanName(entry.getProfile().getName());
                    if (MOO_USERS_NAMES.contains(profileClean)) {
                        if (targetClean.contains(profileClean) || profileClean.contains(targetClean)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean matchesAnyUser(String text) {
        if (text == null || text.isEmpty()) return false;
        Matcher m = USERNAME_PATTERN.matcher(text);
        while (m.find()) {
            String token = m.group().toLowerCase();
            if (MOO_USERS_NAMES.contains(token)) {
                return true;
            }
        }
        for (String registered : MOO_USERS_NAMES) {
            if (text.contains(registered) || registered.contains(text)) {
                return true;
            }
        }
        return false;
    }
}

