package com.devdroggy.megahammer.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Optional;

public class HammerItem extends PickaxeItem {

    public HammerItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    public int getHammerRange(ItemStack stack, String key, int defaultValue) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(key)) { nbt.putInt(key, defaultValue); }
        return nbt.getInt(key);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        Level level = player.level();
        if (level.isClientSide) return false;
        if (player.isShiftKeyDown()) return false;

        HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;

            int up = getHammerRange(itemstack, "RangeUp", 1);
            int down = getHammerRange(itemstack, "RangeDown", 1);
            int left = getHammerRange(itemstack, "RangeLeft", 1);
            int right = getHammerRange(itemstack, "RangeRight", 1);
            int depth = getHammerRange(itemstack, "RangeDepth", 0);

            // ดึงสถานะพลังอัปเกรดทั้งหมด!
            boolean isAutoSmelt = getHammerRange(itemstack, "AutoSmelt", 0) == 2;
            boolean isMagnet = getHammerRange(itemstack, "Magnet", 0) == 2;
            boolean isVoid = getHammerRange(itemstack, "Void", 0) == 2;
            boolean isUnbreakable = getHammerRange(itemstack, "Durability", 0) == 2;

            mineCustomArea(itemstack, (ServerLevel) level, pos, player, blockHit.getDirection(),
                    up, down, left, right, depth, isAutoSmelt, isMagnet, isVoid, isUnbreakable);
        }
        return true;
    }

    private void mineCustomArea(ItemStack stack, ServerLevel level, BlockPos centerPos, Player player, Direction sideHit,
                                int up, int down, int left, int right, int depth,
                                boolean isAutoSmelt, boolean isMagnet, boolean isVoid, boolean isUnbreakable) {
        Direction playerFacing = player.getDirection();
        Direction upDir, downDir, leftDir, rightDir;

        if (sideHit.getAxis() == Direction.Axis.Y) {
            upDir = playerFacing; downDir = playerFacing.getOpposite();
            leftDir = playerFacing.getCounterClockWise(); rightDir = playerFacing.getClockWise();
        } else {
            upDir = Direction.UP; downDir = Direction.DOWN;
            leftDir = sideHit.getClockWise(); rightDir = sideHit.getCounterClockWise();
        }
        Direction depthDir = sideHit.getOpposite();

        boolean anythingBroke = false;

        for (int d = 0; d <= depth; d++) {
            for (int h = -left; h <= right; h++) {
                for (int v = -down; v <= up; v++) {
                    BlockPos targetPos = centerPos.relative(depthDir, d).relative(h > 0 ? rightDir : leftDir, Math.abs(h)).relative(v > 0 ? upDir : downDir, Math.abs(v));
                    BlockState targetState = level.getBlockState(targetPos);

                    if (!targetState.isAir() && targetState.getDestroySpeed(level, targetPos) >= 0) {
                        List<ItemStack> drops = Block.getDrops(targetState, level, targetPos, level.getBlockEntity(targetPos), player, stack);
                        anythingBroke = true; // บันทึกว่ามีของแตก

                        for (ItemStack drop : drops) {
                            if (isVoid) {
                                Item item = drop.getItem();
                                if (item == Items.COBBLESTONE || item == Items.DIRT || item == Items.GRAVEL || item == Items.NETHERRACK) {
                                    continue;
                                }
                            }
                            ItemStack finalDrop = drop;
                            if (isAutoSmelt) {
                                SimpleContainer container = new SimpleContainer(drop);
                                Optional<SmeltingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level);
                                if (recipe.isPresent()) {
                                    finalDrop = recipe.get().getResultItem(level.registryAccess()).copy();
                                    finalDrop.setCount(drop.getCount() * finalDrop.getCount());
                                }
                            }
                            if (isMagnet) {
                                if (!player.getInventory().add(finalDrop)) { Block.popResource(level, targetPos, finalDrop); }
                            } else { Block.popResource(level, targetPos, finalDrop); }
                        }

                        // --- NEW: แทนที่จะ destroyBlock ให้ใช้ setBlock เพื่อไม่ให้มีเสียงซ้อนกัน ---
                        // Flag 3 คือการสั่งให้เกม update neighbor และ update state แต่ห้ามเล่น sound
                        level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);

                        // --- NEW: แทนที่จะ destroyBlock ให้ใช้ setBlock เพื่อไม่ให้มีเสียงซ้อนกัน ---
                        // Flag 3 คือการสั่งให้เกม update neighbor และ update state แต่ห้ามเล่น sound
                        level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);

                        // Spawn only visual particles without triggering any block breaking sound
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, targetState),
                                targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                                5, 0.25, 0.25, 0.25, 0.05);
                        
                        if (!isUnbreakable) {
                            stack.hurtAndBreak(1, player, (e) -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                        }
                    }
                }
            }
        }

        // --- NEW: หลังจากวน loop เสร็จ ค่อยมาเล่นเสียงตูมเปรียงเดียวที่ตรงกลาง ---
        if (anythingBroke) {
            player.level().playSound(null, centerPos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}