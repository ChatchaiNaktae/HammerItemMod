package com.devdroggy.megahammer.network;

import com.devdroggy.megahammer.MegaHammer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(MegaHammer.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(HammerConfigPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(HammerConfigPacket::new)
                .encoder(HammerConfigPacket::toBytes)
                .consumerMainThread(HammerConfigPacket::handle)
                .add();

        net.messageBuilder(HammerUpgradePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(HammerUpgradePacket::new)
                .encoder(HammerUpgradePacket::toBytes)
                .consumerMainThread(HammerUpgradePacket::handle)
                .add();
    }

    // Helper method to send packets from the GUI
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}