package com.sakurafuld.kataklysm.mixin.solar;

import com.sakurafuld.kataklysm.common.capability.SolarChunkProvider;
import mekanism.api.math.FloatingLong;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityAdvancedSolarGenerator.class, remap = false)
public abstract class AdvancedSolarGeneratorMixin extends SolarGeneratorMixin{


    @Inject(method = "checkCanSeeSun()Z", at = @At("HEAD"), remap = false, cancellable = true)
    private void check(CallbackInfoReturnable<Boolean> cl){
        TileEntityAdvancedSolarGenerator self = (TileEntityAdvancedSolarGenerator) ((Object) this);
        if(self.hasLevel()){
            self.getLevel().getChunkAt(self.getBlockPos()).getCapability(SolarChunkProvider.SOLAR)
                    .ifPresent(solar->{
                        if(solar.isActive()) cl.setReturnValue(true);
                    });
        }

    }

    @Inject(method = "recheckSettings()V", at = @At("HEAD"), remap = false)
    private void calculate(CallbackInfo ci){
        TileEntityAdvancedSolarGenerator self = (TileEntityAdvancedSolarGenerator) ((Object) this);
        if(self.hasLevel()){
            Biome b = self.getLevel().getBiomeManager().getBiome(self.getBlockPos()).value();
            boolean rain = b.getPrecipitation() != Biome.Precipitation.NONE;
            float tempEff = 0.3F * (0.8F - b.getBaseTemperature());
            float humidityEff = rain ? -0.3F * b.getDownfall() : 0.0F;
            multiplier = 1.0F + tempEff + humidityEff;
        }
    }

    @Inject(method = "getProduction()Lmekanism/api/math/FloatingLong;", at = @At("HEAD"), cancellable = true, remap = false)
    private void production(CallbackInfoReturnable<FloatingLong> cir){
        TileEntityAdvancedSolarGenerator self = (TileEntityAdvancedSolarGenerator) ((Object) this);
        if(self.hasLevel()){
            self.getLevel().getChunkAt(self.getBlockPos()).getCapability(SolarChunkProvider.SOLAR)
                    .ifPresent(solar->{
                        if(solar.isActive()){
                            float brightness = 120F;//
                            cir.setReturnValue(MekanismGeneratorsConfig.generators.advancedSolarGeneration.get().multiply(this.multiplier * brightness));
                        }

                    });
        }

    }
}
