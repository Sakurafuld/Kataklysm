package com.sakurafuld.kataklysm.common.capability;

import net.minecraft.core.BlockPos;


public class SolarChunk {
    private BlockPos active = null;

    public BlockPos getActive() {
        return active;
    }
    public boolean setActive(BlockPos active) {
        if(active != null && this.isActive())
            return false;

        this.active = active;
        return true;
    }
    public boolean isActive(){
        return this.active != null;
    }
}