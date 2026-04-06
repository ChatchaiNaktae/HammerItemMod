package com.devdroggy.megahammer.capability;

import net.minecraft.nbt.CompoundTag;

public class HammerUpgrades {
    // เก็บสถานะ: 0=Locked, 1=OFF, 2=ON
    public int smelt, magnet, voidJunk, durability;

    public void copyFrom(HammerUpgrades source) {
        this.smelt = source.smelt;
        this.magnet = source.magnet;
        this.voidJunk = source.voidJunk;
        this.durability = source.durability;
    }

    public void saveNBT(CompoundTag nbt) {
        nbt.putInt("smelt", smelt);
        nbt.putInt("magnet", magnet);
        nbt.putInt("voidJunk", voidJunk);
        nbt.putInt("durability", durability);
    }

    public void loadNBT(CompoundTag nbt) {
        smelt = nbt.getInt("smelt");
        magnet = nbt.getInt("magnet");
        voidJunk = nbt.getInt("voidJunk");
        durability = nbt.getInt("durability");
    }
}