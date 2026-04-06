package com.devdroggy.megahammer.client;

import com.devdroggy.megahammer.client.renderer.ModRenderTypes;
import com.devdroggy.megahammer.config.ModConfig;
import com.devdroggy.megahammer.item.HammerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "megahammer", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BlockHighlightRenderer {
    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.level() == null) return;
        Level level = player.level();

        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof HammerItem hammerItem)) return;
        if (player.isShiftKeyDown()) return;

        BlockHitResult blockHit = event.getTarget();
        if (blockHit.getType() != HitResult.Type.BLOCK) return;

        BlockPos centerPos = blockHit.getBlockPos();
        Direction sideHit = blockHit.getDirection();

        int up = hammerItem.getHammerRange(mainHand, "RangeUp", 1);
        int down = hammerItem.getHammerRange(mainHand, "RangeDown", 1);
        int left = hammerItem.getHammerRange(mainHand, "RangeLeft", 1);
        int right = hammerItem.getHammerRange(mainHand, "RangeRight", 1);
        int depth = hammerItem.getHammerRange(mainHand, "RangeDepth", 0);

        Direction playerFacing = player.getDirection();
        Direction upDir, downDir, leftDir, rightDir;

        if (sideHit.getAxis() == Direction.Axis.Y) {
            upDir = playerFacing;
            downDir = playerFacing.getOpposite();
            leftDir = playerFacing.getCounterClockWise();
            rightDir = playerFacing.getClockWise();
        } else {
            upDir = Direction.UP;
            downDir = Direction.DOWN;
            leftDir = sideHit.getClockWise();
            rightDir = sideHit.getCounterClockWise();
        }
        Direction depthDir = sideHit.getOpposite();

        Camera camera = event.getCamera();
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;
        PoseStack poseStack = event.getPoseStack();

        List<AABB> boxesToRender = new ArrayList<>();
        AABB giantBox = null;

        for (int d = 0; d <= depth; d++) {
            for (int h = -left; h <= right; h++) {
                for (int v = -down; v <= up; v++) {
                    BlockPos targetPos = centerPos
                            .relative(depthDir, d)
                            .relative(h > 0 ? rightDir : leftDir, Math.abs(h))
                            .relative(v > 0 ? upDir : downDir, Math.abs(v));

                    if (level.isLoaded(targetPos)) {
                        BlockState targetState = level.getBlockState(targetPos);

                        if (!targetState.isAir() && targetState.getDestroySpeed(level, targetPos) >= 0) {
                            AABB box = new AABB(targetPos);
                            boxesToRender.add(box);

                            if (giantBox == null) {
                                giantBox = box;
                            } else {
                                giantBox = giantBox.minmax(box);
                            }
                        }
                    }
                }
            }
        }

        if (boxesToRender.isEmpty() || giantBox == null) return;

        boolean isGrid = ModConfig.RENDER_BLOCK_GRID.get();

        if (isGrid) {
            // --- 🌟 แก้บั๊กตรงนี้: ขอคิววาดทึบ วาดให้เสร็จก่อน ---
            VertexConsumer solidConsumer = event.getMultiBufferSource().getBuffer(ModRenderTypes.TRANSLUCENT_BOX);
            for (AABB box : boxesToRender) {
                AABB renderBox = box.inflate(0.005D).move(-camX, -camY, -camZ);
                drawFilledBox(poseStack, solidConsumer, renderBox, 1.0F, 0.0F, 0.0F, 0.3F);
            }

            // --- แล้วค่อยขอคิววาดเส้นขอบ ---
            VertexConsumer lineConsumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
            for (AABB box : boxesToRender) {
                AABB renderBox = box.inflate(0.005D).move(-camX, -camY, -camZ);
                LevelRenderer.renderLineBox(poseStack, lineConsumer, renderBox, 1.0F, 0.0F, 0.0F, 0.8F);
            }
        } else {
            // --- ทำแบบเดียวกันกับโหมด Giant Box ---
            VertexConsumer solidConsumer = event.getMultiBufferSource().getBuffer(ModRenderTypes.TRANSLUCENT_BOX);
            AABB renderBox = giantBox.inflate(0.005D).move(-camX, -camY, -camZ);
            drawFilledBox(poseStack, solidConsumer, renderBox, 1.0F, 0.0F, 0.0F, 0.3F);

            VertexConsumer lineConsumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
            LevelRenderer.renderLineBox(poseStack, lineConsumer, renderBox, 1.0F, 0.0F, 0.0F, 0.8F);
        }

        event.setCanceled(true);
    }

    private static void drawFilledBox(PoseStack poseStack, VertexConsumer consumer, AABB box, float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();
        float minX = (float) box.minX; float minY = (float) box.minY; float minZ = (float) box.minZ;
        float maxX = (float) box.maxX; float maxY = (float) box.maxY; float maxZ = (float) box.maxZ;

        // Down Face
        consumer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).endVertex();
        // Up Face
        consumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        // North Face
        consumer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).endVertex();
        // South Face
        consumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        // West Face
        consumer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).endVertex();
        // East Face
        consumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).endVertex();
    }
}