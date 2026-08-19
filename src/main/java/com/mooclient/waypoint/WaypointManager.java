package com.mooclient.waypoint;

import com.google.gson.*;
import com.mooclient.MooClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton manager for creating, storing, saving and loading waypoints.
 * Optimized with world-aware caching to eliminate allocations in the render loop.
 */
public class WaypointManager {

    private static WaypointManager instance;
    private final List<Waypoint> waypoints = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("mooclient_waypoints.json");

    // Cache to prevent stream filtering on every frame
    private List<Waypoint> cachedWorldWaypoints = null;
    private String cachedWorldKey = "";

    private WaypointManager() {
        load();
    }

    public static WaypointManager getInstance() {
        if (instance == null) {
            instance = new WaypointManager();
        }
        return instance;
    }

    public List<Waypoint> getAllWaypoints() {
        return waypoints;
    }

    public void invalidateCache() {
        this.cachedWorldWaypoints = null;
        this.cachedWorldKey = "";
    }

    /**
     * Gets all waypoints active for the current dimension and current server/world.
     * Uses cached results for max performance ($O(1)$ lookup per frame).
     */
    public List<Waypoint> getWaypointsForCurrentWorld(MinecraftClient client) {
        if (client == null || client.world == null) return Collections.emptyList();

        String currentDim = getCurrentDimension(client);
        String currentServer = getCurrentServerOrWorld(client);
        String currentKey = currentDim + "@" + currentServer;

        if (cachedWorldWaypoints != null && currentKey.equals(cachedWorldKey)) {
            return cachedWorldWaypoints;
        }

        List<Waypoint> filtered = waypoints.stream()
                .filter(w -> matchesDimension(w.getDimension(), currentDim))
                .filter(w -> matchesServer(w.getServerOrWorld(), currentServer))
                .collect(Collectors.toList());

        this.cachedWorldWaypoints = filtered;
        this.cachedWorldKey = currentKey;
        return filtered;
    }

    public static boolean matchesDimension(String wpDim, String currentDim) {
        if (wpDim == null || wpDim.isEmpty() || wpDim.equalsIgnoreCase("all")) return true;
        if (currentDim == null || currentDim.isEmpty()) return true;
        String d1 = cleanDim(wpDim);
        String d2 = cleanDim(currentDim);
        return d1.equalsIgnoreCase(d2);
    }

    private static String cleanDim(String d) {
        if (d == null) return "";
        d = d.trim().toLowerCase();
        if (d.startsWith("minecraft:")) d = d.substring("minecraft:".length());
        return d;
    }

    public static boolean matchesServer(String wpServer, String currentServer) {
        if (wpServer == null || wpServer.isEmpty() || wpServer.equalsIgnoreCase("global")) return true;
        if (currentServer == null || currentServer.isEmpty() || currentServer.equalsIgnoreCase("global")) return true;
        String s1 = cleanServer(wpServer);
        String s2 = cleanServer(currentServer);
        return s1.equalsIgnoreCase(s2) || s1.contains(s2) || s2.contains(s1);
    }

    private static String cleanServer(String s) {
        if (s == null) return "";
        s = s.trim().toLowerCase();
        if (s.contains(":")) s = s.substring(0, s.indexOf(':'));
        return s;
    }

    public void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
        invalidateCache();
        save();
    }

    public void removeWaypoint(String id) {
        waypoints.removeIf(w -> w.getId().equals(id));
        invalidateCache();
        save();
    }

    public void toggleWaypoint(String id) {
        for (Waypoint w : waypoints) {
            if (w.getId().equals(id)) {
                w.setVisible(!w.isVisible());
                break;
            }
        }
        invalidateCache();
        save();
    }

    /**
     * Automatically creates or updates a Death Waypoint at the location of player's demise.
     */
    public void createDeathWaypoint(double x, double y, double z, World world) {
        if (world == null) return;

        String dim = world.getRegistryKey().getValue().toString();
        MinecraftClient client = MinecraftClient.getInstance();
        String server = getCurrentServerOrWorld(client);

        // Remove any previous death waypoint so we always have only the most recent one
        waypoints.removeIf(w -> "death_waypoint".equals(w.getId()) || "Śmierć".equalsIgnoreCase(w.getName()) || "Death".equalsIgnoreCase(w.getName()));

        Waypoint deathWp = new Waypoint(
                "death_waypoint",
                "Śmierć",
                Math.round(x * 10.0) / 10.0,
                Math.round(y * 10.0) / 10.0,
                Math.round(z * 10.0) / 10.0,
                dim,
                server,
                0xFFFF2222, // Crimson Red marker
                true,
                true
        );

        // Insert at the beginning of the list for quick access
        waypoints.add(0, deathWp);
        invalidateCache();
        save();
    }

    public static String getCurrentDimension(MinecraftClient client) {
        if (client.world == null) return "minecraft:overworld";
        return client.world.getRegistryKey().getValue().toString();
    }

    public static String getCurrentServerOrWorld(MinecraftClient client) {
        if (client.getCurrentServerEntry() != null) {
            return cleanServer(client.getCurrentServerEntry().address);
        }
        if (client.isInSingleplayer() && client.getServer() != null) {
            try {
                return "sp_" + client.getServer().getSaveProperties().getLevelName().toLowerCase();
            } catch (Exception ignored) {}
        }
        return "singleplayer";
    }

    public void save() {
        try {
            JsonArray array = new JsonArray();
            for (Waypoint w : waypoints) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", w.getId());
                obj.addProperty("name", w.getName());
                obj.addProperty("x", w.getX());
                obj.addProperty("y", w.getY());
                obj.addProperty("z", w.getZ());
                obj.addProperty("dimension", w.getDimension());
                obj.addProperty("serverOrWorld", w.getServerOrWorld());
                obj.addProperty("color", w.getColor());
                obj.addProperty("visible", w.isVisible());
                obj.addProperty("beacon", w.isBeacon());
                array.add(obj);
            }

            Files.writeString(FILE_PATH, GSON.toJson(array));
        } catch (IOException e) {
            MooClient.LOGGER.error("Failed to save waypoints", e);
        }
    }

    public void load() {
        waypoints.clear();
        invalidateCache();

        if (!Files.exists(FILE_PATH)) {
            return;
        }

        try {
            String content = Files.readString(FILE_PATH);
            JsonArray array = JsonParser.parseString(content).getAsJsonArray();

            for (JsonElement el : array) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();

                String id = obj.has("id") ? obj.get("id").getAsString() : null;
                String name = obj.has("name") ? obj.get("name").getAsString() : "Waypoint";
                double x = obj.has("x") ? obj.get("x").getAsDouble() : 0;
                double y = obj.has("y") ? obj.get("y").getAsDouble() : 64;
                double z = obj.has("z") ? obj.get("z").getAsDouble() : 0;
                String dim = obj.has("dimension") ? obj.get("dimension").getAsString() : "minecraft:overworld";
                String server = obj.has("serverOrWorld") ? obj.get("serverOrWorld").getAsString() : "global";
                int color = obj.has("color") ? obj.get("color").getAsInt() : 0xFF5555;
                boolean visible = !obj.has("visible") || obj.get("visible").getAsBoolean();
                boolean beacon = !obj.has("beacon") || obj.get("beacon").getAsBoolean();

                waypoints.add(new Waypoint(id, name, x, y, z, dim, server, color, visible, beacon));
            }
        } catch (Exception e) {
            MooClient.LOGGER.error("Failed to load waypoints", e);
        }
    }
}
