package com.devdroggy.megahammer;

import com.devdroggy.megahammer.init.ModCreativeTabs;
import com.devdroggy.megahammer.init.ModItems;
import com.devdroggy.megahammer.network.ModMessages;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MegaHammer.MOD_ID)
public class MegaHammer {
    public static final String MOD_ID = "megahammer";

    public MegaHammer() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, com.devdroggy.megahammer.config.ModConfig.SPEC);

        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
        });
        event.enqueueWork(() -> {
            net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent.class.hashCode();
        });
    }
}