package com.mooclient.network;

import com.google.gson.JsonElement;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 100% non-blocking, asynchronous cross-client player discovery.
 * Runs strictly in the background with ZERO impact on FPS or game rendering.
 */
public class MooNetworkHandler {

    private static final String HUB_ID = "ff8081819ff5b11001a00b7365962e83";
    private static final String HUB_URL = "https://api.restful-api.dev/objects/" + HUB_ID;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static final ScheduledExecutorService ASYNC_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "MooClient-AsyncPresence");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });

    private static final Map<String, Long> KNOWN_USERS = new ConcurrentHashMap<>();
    private static final AtomicBoolean IS_SYNCING = new AtomicBoolean(false);

    public static void init() {
        // Immediate sync in background after game loads
        ASYNC_EXECUTOR.schedule(MooNetworkHandler::triggerAsyncSync, 1000, TimeUnit.MILLISECONDS);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ASYNC_EXECUTOR.schedule(MooNetworkHandler::triggerAsyncSync, 500, TimeUnit.MILLISECONDS);
            ASYNC_EXECUTOR.schedule(MooNetworkHandler::triggerAsyncSync, 3000, TimeUnit.MILLISECONDS);
        });

        // Periodic background sync every 8 seconds (never on render thread)
        ASYNC_EXECUTOR.scheduleAtFixedRate(MooNetworkHandler::triggerAsyncSync, 4000, 8000, TimeUnit.MILLISECONDS);
    }

    public static void sendBroadcast() {
        ASYNC_EXECUTOR.execute(MooNetworkHandler::triggerAsyncSync);
    }

    private static void triggerAsyncSync() {
        if (!IS_SYNCING.compareAndSet(false, true)) {
            return;
        }

        try {
            MinecraftClient client = MinecraftClient.getInstance();
            String myUsername = "";
            if (client.getSession() != null && client.getSession().getUsername() != null) {
                myUsername = client.getSession().getUsername().trim().toLowerCase();
            } else if (client.player != null && client.player.getName() != null) {
                myUsername = client.player.getName().getString().trim().toLowerCase();
            }

            if (!myUsername.isEmpty()) {
                MooUserManager.registerUser(myUsername, client.player != null ? client.player.getUuid() : null);
            }

            final String finalMyUsername = myUsername;

            // 1. GET current online users asynchronously (non-blocking)
            HttpRequest getReq = HttpRequest.newBuilder()
                    .uri(URI.create(HUB_URL))
                    .timeout(Duration.ofSeconds(3))
                    .header("User-Agent", "MooClient")
                    .GET()
                    .build();

            HTTP_CLIENT.sendAsync(getReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        try {
                            if (resp.statusCode() == 200 && resp.body() != null) {
                                JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
                                if (root.has("data") && root.get("data").isJsonObject()) {
                                    JsonObject data = root.getAsJsonObject("data");
                                    long now = System.currentTimeMillis();

                                    for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                                        String user = entry.getKey().trim().toLowerCase();
                                        long timestamp = entry.getValue().getAsLong();

                                        // Keep active users from the last 2 hours
                                        if (now - timestamp < 2 * 3600 * 1000) {
                                            KNOWN_USERS.put(user, timestamp);
                                            MooUserManager.registerUser(user, null);
                                        }
                                    }
                                }
                            }

                            // 2. Put our own heartbeat asynchronously
                            if (!finalMyUsername.isEmpty()) {
                                KNOWN_USERS.put(finalMyUsername, System.currentTimeMillis());

                                JsonObject payload = new JsonObject();
                                payload.addProperty("name", "MooClient_Global_Hub");

                                JsonObject dataObj = new JsonObject();
                                long now = System.currentTimeMillis();
                                for (Map.Entry<String, Long> entry : KNOWN_USERS.entrySet()) {
                                    if (now - entry.getValue() < 2 * 3600 * 1000) {
                                        dataObj.addProperty(entry.getKey(), entry.getValue());
                                    }
                                }
                                payload.add("data", dataObj);

                                HttpRequest putReq = HttpRequest.newBuilder()
                                        .uri(URI.create(HUB_URL))
                                        .timeout(Duration.ofSeconds(3))
                                        .header("Content-Type", "application/json")
                                        .header("User-Agent", "MooClient")
                                        .PUT(HttpRequest.BodyPublishers.ofString(payload.toString()))
                                        .build();

                                HTTP_CLIENT.sendAsync(putReq, HttpResponse.BodyHandlers.discarding())
                                        .whenComplete((r, ex) -> IS_SYNCING.set(false));
                            } else {
                                IS_SYNCING.set(false);
                            }

                        } catch (Exception e) {
                            IS_SYNCING.set(false);
                        }
                    })
                    .exceptionally(ex -> {
                        IS_SYNCING.set(false);
                        return null;
                    });

        } catch (Exception e) {
            IS_SYNCING.set(false);
        }
    }
}
