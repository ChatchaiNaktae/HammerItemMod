package com.devdroggy.megahammer.network;

import com.devdroggy.megahammer.capability.HammerUpgradeProvider;
import com.devdroggy.megahammer.init.ModItems;
import com.devdroggy.megahammer.item.HammerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HammerUpgradePacket {
    private final int upgradeId; // 0=Smelt, 1=Magnet, 2=Void, 3=Durability
    private final int action; // 0=Unlock, 1=Toggle

    public HammerUpgradePacket(int upgradeId, int action) {
        this.upgradeId = upgradeId;
        this.action = action;
    }

    public HammerUpgradePacket(FriendlyByteBuf buf) {
        this.upgradeId = buf.readInt();
        this.action = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(upgradeId);
        buf.writeInt(action);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof HammerItem)) return;

            // Map ID ให้ตรงกับไอเทม Module ที่ต้องใช้
            Item requiredModule = switch(upgradeId) {
                case 1 -> ModItems.MAGNET_MODULE.get();
                case 2 -> ModItems.VOID_MODULE.get();
                case 3 -> ModItems.DURABILITY_MODULE.get();
                default -> ModItems.AUTO_SMELT_MODULE.get();
            };

            // ดึงข้อมูลจากสมอง (Capability) ของผู้เล่นแทนค้อน
            player.getCapability(HammerUpgradeProvider.PLAYER_UPGRADES).ifPresent(upgrades -> {

                int currentState = switch(upgradeId) {
                    case 1 -> upgrades.magnet;
                    case 2 -> upgrades.voidJunk;
                    case 3 -> upgrades.durability;
                    default -> upgrades.smelt;
                };

                if (action == 0 && currentState == 0) { // ขอปลดล็อก
                    boolean isCreative = player.isCreative();
                    boolean hasXp = isCreative || player.experienceLevel >= 30;
                    int moduleSlot = -1;

                    if (!isCreative) {
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            if (player.getInventory().getItem(i).getItem() == requiredModule) {
                                moduleSlot = i;
                                break;
                            }
                        }
                    }

                    // ถ้าของครบ จัดการหักของและบันทึกพลังลงตัวละคร!
                    if (hasXp && (isCreative || moduleSlot != -1)) {
                        if (!isCreative) {
                            player.giveExperienceLevels(-30);
                            player.getInventory().removeItem(moduleSlot, 1);
                        }

                        switch(upgradeId) {
                            case 1 -> upgrades.magnet = 2;
                            case 2 -> upgrades.voidJunk = 2;
                            case 3 -> upgrades.durability = 2;
                            default -> upgrades.smelt = 2;
                        }

                        // เพื่อให้ UI ของค้อนอัปเดตตามทันที เราจะเซฟลง NBT ของค้อนแถมไปด้วย
                        String nbtKey = switch(upgradeId) {
                            case 1 -> "Magnet"; case 2 -> "Void"; case 3 -> "Durability"; default -> "AutoSmelt";
                        };
                        stack.getOrCreateTag().putInt(nbtKey, 2);

                        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
                else if (action == 1 && currentState > 0) { // ขอสลับเปิดปิด
                    int newState = (currentState == 1) ? 2 : 1;
                    switch(upgradeId) {
                        case 1 -> upgrades.magnet = newState;
                        case 2 -> upgrades.voidJunk = newState;
                        case 3 -> upgrades.durability = newState;
                        default -> upgrades.smelt = newState;
                    }

                    String nbtKey = switch(upgradeId) {
                        case 1 -> "Magnet"; case 2 -> "Void"; case 3 -> "Durability"; default -> "AutoSmelt";
                    };
                    stack.getOrCreateTag().putInt(nbtKey, newState);

                    player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.0F);
                }
            });
        });
        return true;
    }
}