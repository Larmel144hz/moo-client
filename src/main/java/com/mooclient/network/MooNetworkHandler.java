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
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Real-time bidirectional cross-client player discovery.
 * Uses native Java HTTP & WebSockets to instantly broadcast and receive Moo Client player presence.
 */
public class MooNetworkHandler {

    private static final String TOPIC = "mooclient_presence_live";
    private static final String WS_URL = "wss://ntfy.sh/" + TOPIC + "/ws";
    private static final String POST_URL = "https://ntfy.sh/" + TOPIC;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "MooClient-Presence");
        t.setDaemon(true);
        return t;
    });

    private static WebSocket webSocket = null;
    private static volatile boolean isWsConnecting = false;

    public static void init() {
        connectWebSocket();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            MooUserManager.clear();
            sendHeartbeat();
            SCHEDULER.schedule(MooNetworkHandler::sendHeartbeat, 1000, TimeUnit.MILLISECONDS);
            SCHEDULER.schedule(MooNetworkHandler::sendHeartbeat, 3000, TimeUnit.MILLISECONDS);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MooUserManager.clear();
        });

        // Periodic presence heartbeat every 4 seconds
        SCHEDULER.scheduleAtFixedRate(MooNetworkHandler::sendHeartbeat, 1000, 4000, TimeUnit.MILLISECONDS);

        // Keep WebSocket alive
        SCHEDULER.scheduleAtFixedRate(MooNetworkHandler::ensureWsConnected, 5000, 10000, TimeUnit.MILLISECONDS);
    }

    public static void sendBroadcast() {
        sendHeartbeat();
    }

    private static synchronized void connectWebSocket() {
        if (isWsConnecting || (webSocket != null && !webSocket.isInputClosed() && !webSocket.isOutputClosed())) {
            return;
        }
        isWsConnecting = true;

        try {
            HTTP_CLIENT.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .buildAsync(URI.create(WS_URL), new WebSocket.Listener() {
                        @Override
                        public void onOpen(WebSocket ws) {
                            webSocket = ws;
                            isWsConnecting = false;
                            ws.request(1);
                        }

                        @Override
                        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                            try {
                                handleIncomingMessage(data.toString());
                            } catch (Exception ignored) {
                            }
                            ws.request(1);
                            return null;
                        }

                        @Override
                        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                            webSocket = null;
                            isWsConnecting = false;
                            return null;
                        }

                        @Override
                        public void onError(WebSocket ws, Throwable error) {
                            webSocket = null;
                            isWsConnecting = false;
                        }
                    });
        } catch (Exception e) {
            isWsConnecting = false;
        }
    }

    private static void ensureWsConnected() {
        if (webSocket == null || webSocket.isInputClosed() || webSocket.isOutputClosed()) {
            connectWebSocket();
        }
    }

    private static void handleIncomingMessage(String rawJson) {
        if (rawJson == null || rawJson.isEmpty()) return;

        try {
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
            if (!root.has("event") || !"message".equals(root.get("event").getAsString())) {
                return;
            }

            if (!root.has("message")) return;
            String innerStr = root.get("message").getAsString();
            if (innerStr == null || innerStr.isEmpty()) return;

            JsonObject data = JsonParser.parseString(innerStr).getAsJsonObject();
            String u = data.has("u") ? data.get("u").getAsString() : null;
            String uuidStr = data.has("uuid") ? data.get("uuid").getAsString() : null;
            String s = data.has("s") ? data.get("s").getAsString() : null;
            long t = data.has("t") ? data.get("t").getAsLong() : 0;

            if (u == null || u.isBlank()) return;

            MinecraftClient client = MinecraftClient.getInstance();
            String myUsername = (client.getSession() != null) ? client.getSession().getUsername() : (client.player != null ? client.player.getName().getString() : "");

            // Ignore our own broadcast
            if (myUsername != null && u.equalsIgnoreCase(myUsername.trim())) {
                return;
            }

            UUID parsedUuid = null;
            if (uuidStr != null && !uuidStr.isBlank()) {
                try {
                    parsedUuid = UUID.fromString(uuidStr);
                } catch (Exception ignored) {}
            }

            if ("offline".equalsIgnoreCase(s)) {
                MooUserManager.unregisterUser(u, parsedUuid);
            } else {
                MooUserManager.registerUser(u, parsedUuid);
            }
        } catch (Exception ignored) {
        }
    }

    public static void sendHeartbeat() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null && client.getSession() == null) {
                return;
            }

            String username = client.getSession() != null ? client.getSession().getUsername() : (client.player != null ? client.player.getName().getString() : "");
            if (username == null || username.trim().isEmpty()) {
                return;
            }

            String server = getCurrentServerAddress();

            String uuidStr = "";
            if (client.player != null && client.player.getUuid() != null) {
                uuidStr = client.player.getUuid().toString();
            } else if (client.getSession() != null && client.getSession().getUuidOrNull() != null) {
                uuidStr = client.getSession().getUuidOrNull().toString();
            }

            long now = System.currentTimeMillis();
            JsonObject payload = new JsonObject();
            payload.addProperty("u", username.trim());
            payload.addProperty("uuid", uuidStr);
            payload.addProperty("s", server);
            payload.addProperty("t", now);

            HttpRequest postReq = HttpRequest.newBuilder()
                    .uri(URI.create(POST_URL))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "text/plain")
                    .header("User-Agent", "MooClient")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HTTP_CLIENT.sendAsync(postReq, HttpResponse.BodyHandlers.discarding());

        } catch (Exception e) {
            MooClient.LOGGER.debug("Heartbeat error: {}", e.getMessage());
        }
    }

    private static String getCurrentServerAddress() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer() || client.getCurrentServerEntry() == null) {
            return "singleplayer";
        }

        String server = client.getCurrentServerEntry().address;
        if (server == null || server.isEmpty()) {
            if (client.getNetworkHandler() != null && client.getNetworkHandler().getConnection() != null) {
                java.net.SocketAddress addr = client.getNetworkHandler().getConnection().getAddress();
                if (addr != null) server = addr.toString();
            }
        }

        if (server == null) return "unknown";

        if (server.contains("/")) {
            server = server.substring(server.lastIndexOf('/') + 1);
        }
        if (server.contains(":")) {
            server = server.substring(0, server.indexOf(':'));
        }
        return server.trim().toLowerCase();
    }
}
