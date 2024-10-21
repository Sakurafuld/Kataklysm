package com.sakurafuld.kataklysm.api;

import dev.ftb.mods.ftbic.util.EnergyItemHandler;
import dev.ftb.mods.ftbic.util.EnergyTier;
import dev.ftb.mods.ftbic.util.FTBICUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemFTBIC extends Item implements EnergyItemHandler {
    private final EnergyTier TIER;
    private final double CAPACITY;

    public ItemFTBIC(Properties prop, EnergyTier t, double cap) {
        super(prop);
        this.TIER = t;
        this.CAPACITY = cap;
    }

    public EnergyTier getEnergyTier(ItemStack stack){
        return this.TIER;
    }
    public double getEnergyCapacity(ItemStack stack) {
        return this.CAPACITY;
    }
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        if (!this.isCreativeEnergyItem()) {
            list.add(FTBICUtils.energyTooltip(stack, this));
        }
    }
    public boolean isBarVisible(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().contains("Energy");
    }
    public int getBarWidth(ItemStack stack) {
        return Math.round((float) Mth.clamp(this.getEnergy(stack) / this.getEnergyCapacity(stack) * 13.0, 0.0, 13.0));
    }
    public int getBarColor(ItemStack stack) {
        return -65536;
    }

}
