package com.devdroggy.megahammer.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    // Builder for constructing the config file
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Config variables
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_DEPTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> UPGRADE_XP_COST;

    static {
        BUILDER.push("Mega Hammer General Settings");

        MAX_RANGE = BUILDER.comment("Maximum range for Up, Down, Left, and Right (Default: 3)")
                .defineInRange("maxRange", 3, 1, 10); // Default 3, Min 1, Max 10

        MAX_DEPTH = BUILDER.comment("Maximum depth for the hammer (Default: 4)")
                .defineInRange("maxDepth", 4, 0, 15); // Default 4, Min 0, Max 15

        UPGRADE_XP_COST = BUILDER.comment("XP Level cost to unlock an upgrade module (Default: 30)")
                .defineInRange("upgradeXpCost", 30, 0, 100); // Default 30, Min 0, Max 100

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}