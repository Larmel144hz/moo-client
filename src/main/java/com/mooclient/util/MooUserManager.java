package com.mooclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks which players on the current server are confirmed Moo Client users.
 * The local player is always recognized as a Moo Client user.
 * Remote players are recognized in real-time when broadcasting on Moo Client.
 */
public class MooUserManager {

    private static final Set<String> MOO_USERS_NAMES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<UUID> MOO_USERS_UUIDS = Collections.synchronizedSet(new HashSet<>());

    public static void registerUser(String username, UUID uuid) {
        if (username != null && !username.trim().isEmpty()) {
            MOO_USERS_NAMES.add(cleanName(username));
        }
        if (uuid != null) {
            MOO_USERS_UUIDS.add(uuid);
        }
    }

    public static void unregisterUser(String username, UUID uuid) {
        if (username != null) {
            MOO_USERS_NAMES.remove(cleanName(username));
        }
        if (uuid != null) {
            MOO_USERS_UUIDS.remove(uuid);
        }
    }

    public static void clear() {
        MOO_USERS_NAMES.clear();
        MOO_USERS_UUIDS.clear();
    }

    private static String cleanName(String name) {
        if (name == null) return "";
        // Strip Minecraft color codes (§a, §f, etc.) and trim
        return name.replaceAll("(?i)§[0-9a-fk-or]", "").trim().toLowerCase();
    }

    /**
     * Checks if the given player is a confirmed Moo Client user.
     */
    public static boolean isMooUser(String playerName, int entityId) {
        if (!com.mooclient.module.modules.NametagsModule.isNametagsEnabled() || !com.mooclient.module.modules.NametagsModule.isShowLogo()) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Local Player
        if (client.player != null) {
            if (client.player.getId() == entityId) {
                return true;
            }
            if (playerName != null && client.getSession() != null) {
                String myClean = cleanName(client.getSession().getUsername());
                String targetClean = cleanName(playerName);
                if (!myClean.isEmpty() && (targetClean.equals(myClean) || targetClean.contains(myClean))) {
                    return true;
                }
            }
        }

        String targetClean = cleanName(playerName);

        // 2. Direct username match in registered Moo users
        if (!targetClean.isEmpty()) {
            if (MOO_USERS_NAMES.contains(targetClean)) {
                return true;
            }
            // Check if nametag contains rank like "[VIP] Steve" or "Steve [12ms]"
            for (String registered : MOO_USERS_NAMES) {
                if (targetClean.contains(registered) || registered.contains(targetClean)) {
                    return true;
                }
            }
        }

        // 3. Check entity in world
        if (client.world != null) {
            Entity entity = client.world.getEntityById(entityId);
            if (entity instanceof PlayerEntity player) {
                if (player.getUuid() != null && MOO_USERS_UUIDS.contains(player.getUuid())) {
                    return true;
                }
                String entityClean = cleanName(player.getNameForScoreboard());
                if (!entityClean.isEmpty() && MOO_USERS_NAMES.contains(entityClean)) {
                    return true;
                }
            }
        }

        // 4. Tab list entry match
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
}
