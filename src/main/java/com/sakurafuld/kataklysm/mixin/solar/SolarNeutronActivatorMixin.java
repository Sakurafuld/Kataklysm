package com.sakurafuld.kataklysm.mixin.solar;

import com.sakurafuld.kataklysm.common.capability.SolarChunkProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

@Mixin(value = TileEntitySolarNeutronActivator.class, remap = false)
public abstract class SolarNeutronActivatorMixin {

    @Redirect(method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At(value = "FIELD", target = "Lmekanism/common/tile/machine/TileEntitySolarNeutronActivator;ejectorComponent:Lmekanism/common/tile/component/TileComponentEjector;", opcode = Opcodes.PUTFIELD), remap = false)
    private void ejector(TileEntitySolarNeutronActivator self, TileComponentEjector value){
            self.ejectorComponent = new TileComponentEjector(self, ()->{
                AtomicReference<Long> ret = new AtomicReference<>(MekanismConfig.general.chemicalAutoEjectRate.getAsLong());
                self.getLevel().getChunkAt(self.getBlockPos()).getCapability(SolarChunkProvider.SOLAR)
                        .ifPresent(solar->{
                            if(solar.isActive()) ret.set(ret.get() * 2);
                        });
                return ret.get();
            });
    }

    @Redirect(method = "canFunction()Z", at = @At(value = "INVOKE", target = "Lmekanism/common/util/WorldUtils;canSeeSun(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"), remap = false)
    private boolean canSeeSun(Level world, BlockPos pos){
        AtomicReference<Boolean> ret = new AtomicReference<>(WorldUtils.canSeeSun(world, pos.above()));
        TileEntitySolarNeutronActivator self = (TileEntitySolarNeutronActivator) ((Object) this);
        self.getLevel().getChunkAt(self.getBlockPos()).getCapability(SolarChunkProvider.SOLAR)
                .ifPresent(solar->{
                    if(solar.isActive()) ret.set(true);
                });
        return ret.get();
    }
    @ModifyVariable(method = "recalculateProductionRate()F", at = @At("STORE"), ordinal = 1, remap = false)
    private float brightness(float brightness){
        AtomicReference<Float> ret = new AtomicReference<>(brightness);
        TileEntitySolarNeutronActivator self = (TileEntitySolarNeutronActivator) ((Object) this);
        self.getLevel().getChunkAt(self.getBlockPos()).getCapability(SolarChunkProvider.SOLAR)
                .ifPresent(solar->{
                    if(solar.isActive()) ret.set(120.0F);
                });
        return ret.get();
    }

    @ModifyConstant(method = "recalculateProductionRate()F", constant = @Constant(floatValue = 0.2F), remap = false)
    private float rain(float rain){
        AtomicReference<Float> ret = new AtomicReference<>(rain);
        TileEntitySolarNeutronActivator self = (TileEntitySolarNeutronActivator) ((Object) this);
        self.getLevel().getChunkAt(self.getBlockPos()).getCapability(SolarChunkProvider.SOLAR)
                .ifPresent(solar->{
                    if(solar.isActive()) ret.set(1.0F);
                });
        return ret.get();
    }

}
