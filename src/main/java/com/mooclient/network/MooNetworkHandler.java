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
            if (!client.isInSingleplayer() && client.getCurrentServerEntry() != null) {
                server = client.getCurrentServerEntry().address.toLowerCase().trim();
            }

            long now = System.currentTimeMillis();
            JsonObject payload = new JsonObject();
            payload.addProperty("u", username.trim());
            payload.addProperty("s", server);
            payload.addProperty("t", now);

            // 1. Send heartbeat
            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://ntfy.sh/" + PRESENCE_TOPIC))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HTTP_CLIENT.sendAsync(postReq, HttpResponse.BodyHandlers.discarding());

            // 2. Query active players in last 90 seconds
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
                                        JsonObject eventObj = JsonParser.parseString(line).getAsJsonObject();
                                        if (eventObj.has("message")) {
                                            String msgRaw = eventObj.get("message").getAsString();
                                            JsonObject data = JsonParser.parseString(msgRaw).getAsJsonObject();

                                            String u = data.has("u") ? data.get("u").getAsString() : null;
                                            String s = data.has("s") ? data.get("s").getAsString() : null;
                                            long t = data.has("t") ? data.get("t").getAsLong() : 0;

                                            if (u != null && !u.equalsIgnoreCase(finalUsername) && s != null && s.equalsIgnoreCase(finalServer)) {
                                                if (current - t < 90000 || t == 0) {
                                                    MooUserManager.registerUser(u, null);
                                                }
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
}
