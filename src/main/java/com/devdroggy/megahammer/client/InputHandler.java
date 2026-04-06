package com.devdroggy.megahammer.client;

import com.devdroggy.megahammer.client.gui.HammerConfigScreen;
import com.devdroggy.megahammer.item.HammerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "megahammer", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InputHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.CONFIG_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player != null && player.isShiftKeyDown()) {
                ItemStack mainHand = player.getMainHandItem();

                if (mainHand.getItem() instanceof HammerItem) {
                    mc.setScreen(new HammerConfigScreen(mainHand));
                }
            }
        }
    }
}