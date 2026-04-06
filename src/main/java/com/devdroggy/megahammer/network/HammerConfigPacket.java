package com.devdroggy.megahammer.network;

import com.devdroggy.megahammer.item.HammerItem; // Make sure this path points to your HammerItem
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HammerConfigPacket {
    private final int up, down, left, right, depth;

    // Constructor for creating the packet from the GUI
    public HammerConfigPacket(int up, int down, int left, int right, int depth) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.depth = depth;
    }

    // Constructor for reading the packet when it arrives at the Server
    public HammerConfigPacket(FriendlyByteBuf buf) {
        this.up = buf.readInt();
        this.down = buf.readInt();
        this.left = buf.readInt();
        this.right = buf.readInt();
        this.depth = buf.readInt();
    }

    // Write the data into the buffer to send over the network
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(up);
        buf.writeInt(down);
        buf.writeInt(left);
        buf.writeInt(right);
        buf.writeInt(depth);
    }

    // What happens when the Server receives this packet
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        // Enqueue work to happen on the main Server thread
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();

                // Check if the player is actually holding the Hammer
                if (stack.getItem() instanceof HammerItem) {
                    CompoundTag nbt = stack.getOrCreateTag();

                    // Save the new values directly into the item's memory!
                    nbt.putInt("RangeUp", up);
                    nbt.putInt("RangeDown", down);
                    nbt.putInt("RangeLeft", left);
                    nbt.putInt("RangeRight", right);
                    nbt.putInt("RangeDepth", depth);
                }
            }
        });
        return true;
    }
}