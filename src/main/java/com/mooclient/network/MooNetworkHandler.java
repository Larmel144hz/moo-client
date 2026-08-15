package com.mooclient.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mooclient.MooClient;
import com.mooclient.util.MooUserManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles ultra-fast cross-server Moo Client player discovery and presence.
 * Broadcasts presence and parses active players using robust Gson JSON parser.
 */
public class MooNetworkHandler {

    private static final String PRESENCE_TOPIC = "mooclient_players_presence_2026";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "MooClient-Presence");
        t.setDaemon(true);
        return t;
    });

    private static volatile boolean isRunning = false;

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            MooUserManager.clear();
            SCHEDULER.schedule(MooNetworkHandler::sendHeartbeatAndFetch, 0, TimeUnit.MILLISECONDS);
            SCHEDULER.schedule(MooNetworkHandler::sendHeartbeatAndFetch, 1000, TimeUnit.MILLISECONDS);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MooUserManager.clear();
        });

        // Run heartbeat every 2.5 seconds
        SCHEDULER.scheduleAtFixedRate(MooNetworkHandler::sendHeartbeatAndFetch, 500, 2500, TimeUnit.MILLISECONDS);
    }

    public static void sendBroadcast() {
        sendHeartbeatAndFetch();
    }

    public static void sendHeartbeatAndFetch() {
        if (isRunning) return;
        isRunning = true;

        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) {
                isRunning = false;
                return;
            }

            String username = client.getSession() != null ? client.getSession().getUsername() : client.player.getName().getString();
            if (username == null || username.trim().isEmpty()) {
                isRunning = false;
                return;
            }

            String server = "singleplayer";
            if (!client.isInSingleplayer()) {
                if (client.getCurrentServerEntry() != null && client.getCurrentServerEntry().address != null) {
                    server = client.getCurrentServerEntry().address;
                } else if (client.getNetworkHandler() != null && client.getNetworkHandler().getConnection() != null) {
                    java.net.SocketAddress addr = client.getNetworkHandler().getConnection().getAddress();
                    if (addr != null) {
                        server = addr.toString();
                    }
                }
            }

            // Normalize server address: strip slashes and ports
            if (server.contains("/")) {
                server = server.substring(server.lastIndexOf('/') + 1);
            }
            if (server.contains(":")) {
                server = server.substring(0, server.indexOf(':'));
            }
            server = server.trim().toLowerCase();

            String uuidStr = "";
            if (client.player != null && client.player.getUuid() != null) {
                uuidStr = client.player.getUuid().toString();
            } else if (client.getSession() != null && client.getSession().getUuidOrNull() != null) {
                uuidStr = client.getSession().getUuidOrNull().toString();
            }

            long now = System.currentTimeMillis();
            JsonObject dataObj = new JsonObject();
            dataObj.addProperty("u", username.trim());
            dataObj.addProperty("uuid", uuidStr);
            dataObj.addProperty("s", server);
            dataObj.addProperty("t", now);

            JsonObject postPayload = new JsonObject();
            postPayload.addProperty("name", "mooclient_presence");
            postPayload.add("data", dataObj);

            // 1. Send heartbeat
            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.restful-api.dev/objects"))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "MooClient")
                    .POST(HttpRequest.BodyPublishers.ofString(postPayload.toString()))
                    .build();

            HTTP_CLIENT.sendAsync(postReq, HttpResponse.BodyHandlers.discarding());

            // 2. Query active players on the same server
            HttpRequest getReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.restful-api.dev/objects"))
                    .timeout(Duration.ofSeconds(3))
                    .header("User-Agent", "MooClient")
                    .GET()
                    .build();

            String finalServer = server;
            String finalUsername = username.trim();
            String finalUuid = uuidStr;

            HTTP_CLIENT.sendAsync(getReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        try {
                            if (res.statusCode() == 200) {
                                String body = res.body();
                                if (body == null || body.isEmpty()) return;

                                com.google.gson.JsonElement parsed = JsonParser.parseString(body);
                                if (parsed.isJsonArray()) {
                                    com.google.gson.JsonArray arr = parsed.getAsJsonArray();
                                    long current = System.currentTimeMillis();
                                    for (com.google.gson.JsonElement elem : arr) {
                                        if (!elem.isJsonObject()) continue;
                                        JsonObject obj = elem.getAsJsonObject();
                                        if (obj.has("name") && "mooclient_presence".equals(obj.get("name").getAsString())) {
                                            if (obj.has("data") && obj.get("data").isJsonObject()) {
                                                JsonObject d = obj.getAsJsonObject("data");
                                                String u = d.has("u") ? d.get("u").getAsString() : null;
                                                String pUuidStr = d.has("uuid") ? d.get("uuid").getAsString() : null;
                                                String s = d.has("s") ? d.get("s").getAsString() : null;
                                                long t = d.has("t") ? d.get("t").getAsLong() : 0;

                                                boolean isSelf = (u != null && u.equalsIgnoreCase(finalUsername)) || (pUuidStr != null && !finalUuid.isEmpty() && pUuidStr.equalsIgnoreCase(finalUuid));

                                                if (!isSelf && u != null) {
                                                    boolean serverMatch = s == null || s.equalsIgnoreCase(finalServer) || s.contains(finalServer) || finalServer.contains(s);
                                                    if (serverMatch && (current - t < 120000 || t == 0)) {
                                                        java.util.UUID parsedUuid = null;
                                                        if (pUuidStr != null && !pUuidStr.isBlank()) {
                                                            try {
                                                                parsedUuid = java.util.UUID.fromString(pUuidStr);
                                                            } catch (Exception ignored) {}
                                                        }
                                                        MooUserManager.registerUser(u, parsedUuid);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        } finally {
                            isRunning = false;
                        }
                    })
                    .exceptionally(e -> {
                        isRunning = false;
                        return null;
                    });

        } catch (Exception e) {
            isRunning = false;
            MooClient.LOGGER.debug("Presence error: {}", e.getMessage());
        }
    }
}
