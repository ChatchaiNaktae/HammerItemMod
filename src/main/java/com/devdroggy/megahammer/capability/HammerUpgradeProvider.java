package com.devdroggy.megahammer.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HammerUpgradeProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<HammerUpgrades> PLAYER_UPGRADES = CapabilityManager.get(new CapabilityToken<>() {});

    private HammerUpgrades upgrades = null;
    private final LazyOptional<HammerUpgrades> optional = LazyOptional.of(this::createHammerUpgrades);

    private HammerUpgrades createHammerUpgrades() {
        if (this.upgrades == null) {
            this.upgrades = new HammerUpgrades();
        }
        return this.upgrades;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_UPGRADES) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createHammerUpgrades().saveNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createHammerUpgrades().loadNBT(nbt);
    }
}