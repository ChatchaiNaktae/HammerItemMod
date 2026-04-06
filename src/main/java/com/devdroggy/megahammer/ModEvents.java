package com.devdroggy.megahammer;

import com.devdroggy.megahammer.capability.HammerUpgradeProvider;
import com.devdroggy.megahammer.capability.HammerUpgrades;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MegaHammer.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(HammerUpgradeProvider.PLAYER_UPGRADES).isPresent()) {
                event.addCapability(new ResourceLocation(MegaHammer.MOD_ID, "properties"), new HammerUpgradeProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();

        event.getOriginal().getCapability(HammerUpgradeProvider.PLAYER_UPGRADES).ifPresent(oldStore -> {
            event.getEntity().getCapability(HammerUpgradeProvider.PLAYER_UPGRADES).ifPresent(newStore -> {
                newStore.copyFrom(oldStore);
            });
        });

        event.getOriginal().invalidateCaps();
    }
}