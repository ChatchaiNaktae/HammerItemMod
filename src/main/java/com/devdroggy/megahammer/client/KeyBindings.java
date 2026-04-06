package com.devdroggy.megahammer.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    // Category name in the Controls menu
    public static final String CATEGORY = "key.category.megahammer";
    // Description of the action
    public static final String OPEN_CONFIG_GUI = "key.megahammer.open_config";

    // Define the key mapping for the 'G' key
    public static final KeyMapping CONFIG_KEY = new KeyMapping(
            OPEN_CONFIG_GUI, // Translation key for the key name
            KeyConflictContext.IN_GAME, // Only active while playing in the world
            InputConstants.Type.KEYSYM, // It's a keyboard key
            GLFW.GLFW_KEY_G, // Default key is 'G'
            CATEGORY // The category category it belongs to
    );
}