package com.mooclient.network;

import com.mooclient.MooClient;
import com.mooclient.util.MooUserManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Handles Moo Client network handshake and player discovery protocol.
 */
public class MooNetworkHandler {

    public record MooHandshakePayload(String version, String username) implements CustomPayload {
        public static final CustomPayload.Id<MooHandshakePayload> ID = new CustomPayload.Id<>(Identifier.of("mooclient", "handshake"));
        public static final PacketCodec<RegistryByteBuf, MooHandshakePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MooHandshakePayload::version,
            PacketCodecs.STRING, MooHandshakePayload::username,
            MooHandshakePayload::new
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void init() {
        // Register custom payload for client-server communication
        try {
            PayloadTypeRegistry.playS2C().register(MooHandshakePayload.ID, MooHandshakePayload.CODEC);
            PayloadTypeRegistry.playC2S().register(MooHandshakePayload.ID, MooHandshakePayload.CODEC);

            // Listen for other Moo Client users
            ClientPlayNetworking.registerGlobalReceiver(MooHandshakePayload.ID, (payload, context) -> {
                context.client().execute(() -> {
                    if (payload.username() != null && !payload.username().isEmpty()) {
                        MooUserManager.registerUser(payload.username(), null);
                        MooClient.LOGGER.info("Discovered fellow Moo Client user: {}", payload.username());

                        // Send our handshake back so the other user also sees us immediately
                        sendBroadcast();
                    }
                });
            });

            // When connecting to a world or server, clear old users and send handshake
            ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
                MooUserManager.clear();
                if (client.player != null) {
                    MooUserManager.registerUser(client.player.getName().getString(), client.player.getUuid());
                }
                sendBroadcast();
            });

            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                MooUserManager.clear();
            });

        } catch (Exception e) {
            MooClient.LOGGER.warn("Could not register Moo Client network payload: {}", e.getMessage());
        }
    }

    public static void sendBroadcast() {
        try {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            if (client.world != null && ClientPlayNetworking.canSend(MooHandshakePayload.ID)) {
                String name = client.player != null ? client.player.getName().getString() : client.getSession().getUsername();
                ClientPlayNetworking.send(new MooHandshakePayload(MooClient.VERSION, name));
            }
        } catch (Exception ignored) {}
    }
}
