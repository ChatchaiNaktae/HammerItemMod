package com.devdroggy.megahammer.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class ModRenderTypes extends RenderType {
    // Dummy constructor to satisfy inheritance
    public ModRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    // Create a custom render type for a translucent solid box (like Building Gadgets)
    public static final RenderType TRANSLUCENT_BOX = create("translucent_box",
            DefaultVertexFormat.POSITION_COLOR, // We only need X,Y,Z positions and RGBA colors
            VertexFormat.Mode.QUADS, // We are drawing 4-sided faces (Quadrilaterals)
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY) // Allows alpha blending
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST) // Respects blocks in front of it
                    .setCullState(RenderStateShard.NO_CULL) // Draw both inside and outside of the box
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP) // Ignore game lighting, make it glow
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));
}