package com.mooclient.network;

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
 * Broadcasts presence and fetches active players on the same server every 2.5 seconds.
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
        // Clear on join/disconnect and trigger instant discovery
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            MooUserManager.clear();
            // Immediate instant broadcast on join
            SCHEDULER.schedule(MooNetworkHandler::sendHeartbeatAndFetch, 0, TimeUnit.MILLISECONDS);
            SCHEDULER.schedule(MooNetworkHandler::sendHeartbeatAndFetch, 1000, TimeUnit.MILLISECONDS);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MooUserManager.clear();
        });

        // Run ultra-fast heartbeat every 2.5 seconds for instant badge updates
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
            if (!client.isInSingleplayer() && client.getCurrentServerEntry() != null) {
                server = client.getCurrentServerEntry().address.toLowerCase().trim();
            }

            long now = System.currentTimeMillis();
            String payload = String.format("{\"u\":\"%s\",\"s\":\"%s\",\"t\":%d}", username.trim(), server, now);

            // 1. Send heartbeat
            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://ntfy.sh/" + PRESENCE_TOPIC))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HTTP_CLIENT.sendAsync(postReq, HttpResponse.BodyHandlers.discarding());

            // 2. Fetch active players in last 90s (query last 90s so we instantly see existing players)
            HttpRequest getReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://ntfy.sh/" + PRESENCE_TOPIC + "/json?poll=1&since=90s"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            String finalServer = server;
            String finalUsername = username.trim();
            HTTP_CLIENT.sendAsync(getReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> {
                        try {
                            if (res.statusCode() == 200) {
                                String body = res.body();
                                if (body == null || body.isEmpty()) return;

                                String[] lines = body.split("\n");
                                long current = System.currentTimeMillis();

                                for (String line : lines) {
                                    if (line == null || line.isBlank()) continue;
                                    try {
                                        int msgIdx = line.indexOf("\"message\":\"");
                                        if (msgIdx != -1) {
                                            String unescaped = line.substring(msgIdx + 11);
                                            int endIdx = unescaped.indexOf("\"}");
                                            if (endIdx == -1) endIdx = unescaped.indexOf("\"");
                                            if (endIdx != -1) {
                                                String jsonMsg = unescaped.substring(0, endIdx).replace("\\\"", "\"");
                                                parseAndRegisterPlayer(jsonMsg, finalServer, finalUsername, current);
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                }
                            }
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

    private static void parseAndRegisterPlayer(String json, String myServer, String myUsername, long now) {
        try {
            String u = extractJsonField(json, "u");
            String s = extractJsonField(json, "s");
            String tStr = extractJsonField(json, "t");

            if (u != null && !u.equalsIgnoreCase(myUsername) && s != null && s.equalsIgnoreCase(myServer)) {
                long t = tStr != null ? Long.parseLong(tStr) : 0;
                // Cache for 90 seconds so badges appear instantly and never flicker
                if (now - t < 90000 || t == 0) {
                    MooUserManager.registerUser(u, null);
                }
            }
        } catch (Exception ignored) {}
    }

    private static String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start != -1) {
            int end = json.indexOf("\"", start + key.length());
            if (end != -1) {
                return json.substring(start + key.length(), end);
            }
        }
        String numKey = "\"" + field + "\":";
        int numStart = json.indexOf(numKey);
        if (numStart != -1) {
            int end = json.indexOf(",", numStart + numKey.length());
            if (end == -1) end = json.indexOf("}", numStart + numKey.length());
            if (end != -1) {
                return json.substring(numStart + numKey.length(), end).trim();
            }
        }
        return null;
    }
}
