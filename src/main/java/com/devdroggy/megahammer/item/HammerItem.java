package com.devdroggy.megahammer.item;

import com.devdroggy.megahammer.capability.HammerUpgradeProvider;
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

    // ดึงค่า Range (ขนาดกว้างยาว) จากตัวค้อนเหมือนเดิม
    public int getHammerRange(ItemStack stack, String key, int defaultValue) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(key)) { nbt.putInt(key, defaultValue); }
        return nbt.getInt(key);
    }

    // --- ใส่เพิ่มเข้าไปในคลาส HammerItem ---
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);

        // ทำงานเฉพาะฝั่ง Server และเฉพาะตอนที่ผู้เล่นเป็นคนถือค้อน
        if (!level.isClientSide && entity instanceof Player player) {
            // ดึงสมอง (Capability) ของผู้เล่นมาเช็ค
            player.getCapability(com.devdroggy.megahammer.capability.HammerUpgradeProvider.PLAYER_UPGRADES).ifPresent(upgrades -> {
                CompoundTag nbt = stack.getOrCreateTag();
                boolean isChanged = false;

                // เช็คว่า NBT ของค้อน ตรงกับ Capability ของคนถือไหม?
                // ถ้าไม่ตรง (เช่น ค้อนเพิ่งคราฟต์มาใหม่) ให้ก็อปปี้พลังใส่ค้อนทันที
                if (nbt.getInt("AutoSmelt") != upgrades.smelt) { nbt.putInt("AutoSmelt", upgrades.smelt); isChanged = true; }
                if (nbt.getInt("Magnet") != upgrades.magnet) { nbt.putInt("Magnet", upgrades.magnet); isChanged = true; }
                if (nbt.getInt("Void") != upgrades.voidJunk) { nbt.putInt("Void", upgrades.voidJunk); isChanged = true; }
                if (nbt.getInt("Durability") != upgrades.durability) { nbt.putInt("Durability", upgrades.durability); isChanged = true; }

                // ถ้ามีการอัปเดตข้อมูลใหม่ บังคับให้ Server ส่งข้อมูลค้อนนี้ไปอัปเดตหน้าจอ Client ทันที
                if (isChanged) {
                    player.getInventory().setItem(itemSlot, stack);
                }
            });
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        Level level = player.level();
        if (level.isClientSide) return false;

        // ถามระบบ Capability ว่าผู้เล่นคนนี้มีพลังอะไรติดตัวบ้าง (ใช้ .map จัดการ)
        return player.getCapability(HammerUpgradeProvider.PLAYER_UPGRADES).map(upgrades -> {

            boolean isUnbreakable = upgrades.durability == 2;

            // --- ระบบ Safety Lock ---
            if (!isUnbreakable && itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[!] Your hammer is broken! Repair it with Diamond in Anvil to continue using it."), true);
                return true;
            }

            if (player.isShiftKeyDown()) return false;

            HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hit;

                // ดึงขนาดขุดจาก NBT ของค้อน
                int up = getHammerRange(itemstack, "RangeUp", 1);
                int down = getHammerRange(itemstack, "RangeDown", 1);
                int left = getHammerRange(itemstack, "RangeLeft", 1);
                int right = getHammerRange(itemstack, "RangeRight", 1);
                int depth = getHammerRange(itemstack, "RangeDepth", 0);

                // ดึงพลังอัปเกรดจาก สมองผู้เล่น (Capability)
                boolean isAutoSmelt = upgrades.smelt == 2;
                boolean isMagnet = upgrades.magnet == 2;
                boolean isVoid = upgrades.voidJunk == 2;

                mineCustomArea(itemstack, (ServerLevel) level, pos, player, blockHit.getDirection(),
                        up, down, left, right, depth, isAutoSmelt, isMagnet, isVoid, isUnbreakable);
            }
            return true;
        }).orElse(false); // ถ้าดึง Capability ไม่ได้ ให้คืนค่า false (ขุดธรรมดา)
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

                        // เช็ค Safety Lock กลางลูป
                        if (!isUnbreakable && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[!] Your hammer is broken! Repair it with Diamond in Anvil to continue using it."), true);
                            if (anythingBroke) {
                                player.level().playSound(null, centerPos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return;
                        }

                        List<ItemStack> drops = Block.getDrops(targetState, level, targetPos, level.getBlockEntity(targetPos), player, stack);
                        anythingBroke = true;

                        for (ItemStack drop : drops) {
                            ItemStack finalDrop = drop;

                            if (isAutoSmelt) {
                                SimpleContainer container = new SimpleContainer(drop);
                                Optional<SmeltingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level);
                                if (recipe.isPresent()) {
                                    finalDrop = recipe.get().getResultItem(level.registryAccess()).copy();
                                    finalDrop.setCount(drop.getCount() * finalDrop.getCount());
                                }
                            }

                            if (isVoid) {
                                Item item = finalDrop.getItem();
                                if (item == Items.COBBLESTONE || item == Items.COBBLED_DEEPSLATE ||
                                        item == Items.DIRT || item == Items.GRAVEL || item == Items.NETHERRACK ||
                                        item == Items.ANDESITE || item == Items.DIORITE || item == Items.GRANITE ||
                                        item == Items.TUFF || item == Items.DEEPSLATE) {

                                    continue;
                                }
                            }

                            if (isMagnet) {
                                if (!player.getInventory().add(finalDrop)) { Block.popResource(level, targetPos, finalDrop); }
                            } else { Block.popResource(level, targetPos, finalDrop); }
                        }

                        level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);

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

        if (anythingBroke) {
            player.level().playSound(null, centerPos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}