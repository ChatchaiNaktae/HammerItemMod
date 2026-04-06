package com.devdroggy.megahammer;

import com.devdroggy.megahammer.init.ModItems;
import com.devdroggy.megahammer.network.ModMessages;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MegaHammer.MOD_ID)
public class MegaHammer {
    public static final String MOD_ID = "megahammer";

    public MegaHammer() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
        });
        event.enqueueWork(() -> {
            net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent.class.hashCode(); // กระตุ้นการโหลด
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.MEGA_HAMMER);
            event.accept(ModItems.AUTO_SMELT_MODULE);
            event.accept(ModItems.MAGNET_MODULE);
            event.accept(ModItems.VOID_MODULE);
            event.accept(ModItems.DURABILITY_MODULE);
        }
    }
}