package com.devdroggy.megahammer.init;

import com.devdroggy.megahammer.MegaHammer;
import com.devdroggy.megahammer.item.HammerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MegaHammer.MOD_ID);

    public static final RegistryObject<Item> MEGA_HAMMER = ITEMS.register("mega_hammer",
            () -> new HammerItem(Tiers.DIAMOND, 5, -3.0F, new Item.Properties().durability(1561)));

    public static final RegistryObject<Item> AUTO_SMELT_MODULE = ITEMS.register("auto_smelt_module",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MAGNET_MODULE = ITEMS.register("magnet_module",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VOID_MODULE = ITEMS.register("void_module",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DURABILITY_MODULE = ITEMS.register("durability_module",
            () -> new Item(new Item.Properties()));
}