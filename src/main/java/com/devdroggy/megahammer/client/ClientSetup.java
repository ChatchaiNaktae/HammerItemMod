package com.devdroggy.megahammer.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// This class only runs on the physical client (not the dedicated server)
@Mod.EventBusSubscriber(modid = "assets", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        // Register our custom key mapping to the game
        event.register(KeyBindings.CONFIG_KEY);
    }
}