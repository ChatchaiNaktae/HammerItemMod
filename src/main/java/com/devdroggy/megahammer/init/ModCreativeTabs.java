package com.devdroggy.megahammer.init;

import com.devdroggy.megahammer.MegaHammer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MegaHammer.MOD_ID);

    // สร้างแท็บใหม่ ตั้งชื่อว่า "Mega Hammer" และให้แสดงรูปค้อนเป็นไอคอนแท็บ
    public static final RegistryObject<CreativeModeTab> MEGA_HAMMER_TAB = CREATIVE_MODE_TABS.register("megahammer_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> ModItems.MEGA_HAMMER.get().getDefaultInstance())
                    .title(Component.translatable("creativetab.megahammer_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        // ยัดไอเทมทั้งหมดของเราลงไปในแท็บนี้
                        pOutput.accept(ModItems.MEGA_HAMMER.get());
                        pOutput.accept(ModItems.AUTO_SMELT_MODULE.get());
                        pOutput.accept(ModItems.MAGNET_MODULE.get());
                        pOutput.accept(ModItems.VOID_MODULE.get());
                        pOutput.accept(ModItems.DURABILITY_MODULE.get());
                    })
                    .build());
}