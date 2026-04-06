package com.devdroggy.megahammer.network;

import com.devdroggy.megahammer.init.ModItems;
import com.devdroggy.megahammer.item.HammerItem;
import net.minecraft.nbt.CompoundTag;
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

            CompoundTag nbt = stack.getOrCreateTag();

            // Map ID to Key and Item
            String nbtKey = switch(upgradeId) {
                case 1 -> "Magnet"; case 2 -> "Void"; case 3 -> "Durability"; default -> "AutoSmelt";
            };
            Item requiredModule = switch(upgradeId) {
                case 1 -> ModItems.MAGNET_MODULE.get(); case 2 -> ModItems.VOID_MODULE.get();
                case 3 -> ModItems.DURABILITY_MODULE.get(); default -> ModItems.AUTO_SMELT_MODULE.get();
            };

            int currentState = nbt.getInt(nbtKey);

            if (action == 0 && currentState == 0) {
                boolean isCreative = player.isCreative();
                boolean hasXp = isCreative || player.experienceLevel >= 30;
                int moduleSlot = -1;

                if (!isCreative) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        if (player.getInventory().getItem(i).getItem() == requiredModule) {
                            moduleSlot = i; break;
                        }
                    }
                }

                if (hasXp && (isCreative || moduleSlot != -1)) {
                    if (!isCreative) {
                        player.giveExperienceLevels(-30);
                        player.getInventory().removeItem(moduleSlot, 1);
                    }
                    nbt.putInt(nbtKey, 2); // ปลดล็อกและเปิดใช้งาน
                    player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            else if (action == 1 && currentState > 0) {
                nbt.putInt(nbtKey, currentState == 1 ? 2 : 1); // สลับเปิด/ปิด
                player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        });
        return true;
    }
}