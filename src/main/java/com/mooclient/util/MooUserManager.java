package com.mooclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks which players on the current server are confirmed Moo Client users.
 * The local player is always recognized as a Moo Client user.
 * Other players are recognized when they complete the Moo Client handshake.
 */
public class MooUserManager {

    private static final Set<String> MOO_USERS_NAMES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<UUID> MOO_USERS_UUIDS = Collections.synchronizedSet(new HashSet<>());

    public static void registerUser(String username, UUID uuid) {
        if (username != null && !username.trim().isEmpty()) {
            MOO_USERS_NAMES.add(username.trim().toLowerCase());
        }
        if (uuid != null) {
            MOO_USERS_UUIDS.add(uuid);
        }
    }

    public static void unregisterUser(String username, UUID uuid) {
        if (username != null) {
            MOO_USERS_NAMES.remove(username.trim().toLowerCase());
        }
        if (uuid != null) {
            MOO_USERS_UUIDS.remove(uuid);
        }
    }

    public static void clear() {
        MOO_USERS_NAMES.clear();
        MOO_USERS_UUIDS.clear();
    }

    /**
     * Checks if the given player is a confirmed Moo Client user.
     */
    public static boolean isMooUser(String playerName, int entityId) {
        if (!com.mooclient.module.modules.NametagsModule.isNametagsEnabled() || !com.mooclient.module.modules.NametagsModule.isShowLogo()) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Local Player is always a Moo Client user
        if (client.player != null) {
            if (client.player.getId() == entityId) {
                return true;
            }
            if (playerName != null && client.getSession() != null && playerName.equalsIgnoreCase(client.getSession().getUsername())) {
                return true;
            }
            if (client.player.getUuid() != null && MOO_USERS_UUIDS.contains(client.player.getUuid())) {
                return true;
            }
        }

        // 2. Check registered Moo Client usernames from handshake
        if (playerName != null && MOO_USERS_NAMES.contains(playerName.trim().toLowerCase())) {
            return true;
        }

        // 3. Check registered Moo Client UUIDs in world
        if (client.world != null) {
            Entity entity = client.world.getEntityById(entityId);
            if (entity instanceof PlayerEntity player) {
                if (MOO_USERS_UUIDS.contains(player.getUuid())) {
                    return true;
                }
                if (player.getName() != null && MOO_USERS_NAMES.contains(player.getName().getString().trim().toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }
}
