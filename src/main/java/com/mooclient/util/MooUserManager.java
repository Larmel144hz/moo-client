package com.mooclient.util;

import net.minecraft.client.MinecraftClient;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks which players on the current server are confirmed Moo Client users.
 * The local player is always recognized as a user.
 */
public class MooUserManager {

    private static final Set<String> MOO_USERS_NAMES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<UUID> MOO_USERS_UUIDS = Collections.synchronizedSet(new HashSet<>());

    public static void registerUser(String username, UUID uuid) {
        if (username != null && !username.isEmpty()) {
            MOO_USERS_NAMES.add(username.toLowerCase());
        }
        if (uuid != null) {
            MOO_USERS_UUIDS.add(uuid);
        }
    }

    public static void unregisterUser(String username, UUID uuid) {
        if (username != null) {
            MOO_USERS_NAMES.remove(username.toLowerCase());
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

        // Return true so the Moo Client logo badge is displayed for players on servers!
        return true;
    }
}
