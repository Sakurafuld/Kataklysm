package com.sakurafuld.kataklysm.common.compat.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.IExoflameHeatable;
import vazkii.botania.api.block.IHornHarvestable;
import vazkii.botania.api.block.IWandHUD;
import vazkii.botania.api.block.IWandable;
import vazkii.botania.api.mana.IManaCollisionGhost;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.IManaTrigger;
import vazkii.botania.api.mana.spark.ISparkAttachable;

import java.util.HashMap;
import java.util.Map;

public class BlockEntityWithBotania extends BlockEntity {
    private final Map<Capability<?>, LazyOptional<?>> CAPABILITIES = new HashMap<>();
    protected BlockEntityWithBotania(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        if(this instanceof IManaReceiver instance) this.CAPABILITIES.put(BotaniaForgeCapabilities.MANA_RECEIVER, LazyOptional.of(()-> instance));
        if(this instanceof IManaCollisionGhost instance) this.CAPABILITIES.put(BotaniaForgeCapabilities.MANA_GHOST, LazyOptional.of(()-> instance));
        if(this instanceof IExoflameHeatable instance) this.CAPABILITIES.put(BotaniaForgeCapabilities.EXOFLAME_HEATABLE, LazyOptional.of(()-> instance));
        if(this instanceof IHornHarvestable instance) this.CAPABILITIES.put(BotaniaForgeCapabilities.HORN_HARVEST, LazyOptional.of(()-> instance));
        if(this instanceof IManaTrigger instance) this.CAPABILITIES.put(BotaniaForgeCapabilities.MANA_TRIGGER, LazyOptional.of(()-> instance));
        if(this instanceof ISparkAttachable instance) this.CAPABILITIES.put(BotaniaForgeCapabilities.SPARK_ATTACHABLE, LazyOptional.of(()-> instance));
        if(this instanceof IWandable instance) this.CAPABILITIES.put(BotaniaForgeCapabilities.WANDABLE, LazyOptional.of(()-> instance));
        if(this instanceof IWandHUD instance) this.CAPABILITIES.put(BotaniaForgeClientCapabilities.WAND_HUD, LazyOptional.of(()-> instance));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITIES.containsKey(cap) ? CAPABILITIES.get(cap).cast() : super.getCapability(cap, side);
    }
}
