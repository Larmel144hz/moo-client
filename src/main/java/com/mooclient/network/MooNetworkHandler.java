package com.mooclient.network;

import com.mooclient.MooClient;
import com.mooclient.util.MooUserManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 0ms in-game cross-client presence synchronization via Fabric CustomPayload channels.
 */
public class MooNetworkHandler {

    public record PresencePayload(String username, String uuid) implements CustomPayload {
        public static final CustomPayload.Id<PresencePayload> ID = new CustomPayload.Id<>(Identifier.of("mooclient", "presence"));
        public static final PacketCodec<RegistryByteBuf, PresencePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, PresencePayload::username,
                PacketCodecs.STRING, PresencePayload::uuid,
                PresencePayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "MooClient-Presence");
        t.setDaemon(true);
        return t;
    });

    public static void init() {
        try {
            // Register C2S and S2C payload codecs
            PayloadTypeRegistry.playC2S().register(PresencePayload.ID, PresencePayload.CODEC);
            PayloadTypeRegistry.playS2C().register(PresencePayload.ID, PresencePayload.CODEC);

            // Register receiver for presence packets from other clients/server
            ClientPlayNetworking.registerGlobalReceiver(PresencePayload.ID, (payload, context) -> {
                context.client().execute(() -> {
                    if (payload.username() != null && !payload.username().isEmpty()) {
                        UUID u = null;
                        try {
                            if (payload.uuid() != null && !payload.uuid().isEmpty()) {
                                u = UUID.fromString(payload.uuid());
                            }
                        } catch (Exception ignored) {}
                        MooUserManager.registerUser(payload.username(), u);
                    }
                });
            });
        } catch (Exception e) {
            MooClient.LOGGER.warn("Custom payload registration: {}", e.getMessage());
        }

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            sendPresence();
            SCHEDULER.schedule(MooNetworkHandler::sendPresence, 500, TimeUnit.MILLISECONDS);
            SCHEDULER.schedule(MooNetworkHandler::sendPresence, 1500, TimeUnit.MILLISECONDS);
            SCHEDULER.schedule(MooNetworkHandler::sendPresence, 3000, TimeUnit.MILLISECONDS);
        });

        // Periodic presence broadcast every 3 seconds
        SCHEDULER.scheduleAtFixedRate(MooNetworkHandler::sendPresence, 1000, 3000, TimeUnit.MILLISECONDS);
    }

    public static void sendBroadcast() {
        sendPresence();
    }

    public static void sendPresence() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null) return;

            String username = client.getSession() != null ? client.getSession().getUsername() : client.player.getName().getString();
            if (username == null || username.trim().isEmpty()) return;

            String uuidStr = client.player.getUuid() != null ? client.player.getUuid().toString() : "";

            // Register ourselves locally
            MooUserManager.registerUser(username, client.player.getUuid());

            // Send in-game payload to server / other clients
            if (ClientPlayNetworking.canSend(PresencePayload.ID)) {
                ClientPlayNetworking.send(new PresencePayload(username, uuidStr));
            }
        } catch (Exception ignored) {}
    }
}
