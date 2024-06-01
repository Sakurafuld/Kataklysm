package com.sakurafuld.kataklysm.common.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SolarChunkProvider implements ICapabilityProvider {
    public static Capability<SolarChunk> SOLAR = CapabilityManager.get(new CapabilityToken<>() {});

    private SolarChunk solar = null;
    private final LazyOptional<SolarChunk> CAPABILITY = LazyOptional.of(this::create);

    private SolarChunk create(){
        if(this.solar == null)
            this.solar = new SolarChunk();
        return this.solar;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == SOLAR)
            return this.CAPABILITY.cast();
        return LazyOptional.empty();
    }
}
