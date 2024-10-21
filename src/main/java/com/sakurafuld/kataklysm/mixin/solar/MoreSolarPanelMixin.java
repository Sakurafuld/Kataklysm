package com.sakurafuld.kataklysm.mixin.solar;

import com.sakurafuld.kataklysm.api.capability.SolarLevelChunk;
import mekanism.api.math.FloatingLong;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.gamepoint.solarpanels.tiles.panels.AbstractSolarPanel;

@Pseudo
@Mixin(value = AbstractSolarPanel.class, remap = false)
public abstract class MoreSolarPanelMixin {

    @Inject(method = "checkCanSeeSun()Z", at = @At("HEAD"), remap = false, cancellable = true)
    private void check(CallbackInfoReturnable<Boolean> cl){
        AbstractSolarPanel self = (AbstractSolarPanel) ((Object) this);
        if(self.hasLevel()){
            self.getLevel().getChunkAt(self.getBlockPos()).getCapability(SolarLevelChunk.CAPABILITY)
                    .ifPresent(solar->{
                        if(solar.isActive()) cl.setReturnValue(true);
                    });
        }

    }

    @Shadow
    protected abstract FloatingLong getConfiguredMax();
    protected float multiplier;

    @Inject(method = "recheckSettings()V", at = @At("HEAD"), remap = false)
    private void calculate(CallbackInfo ci){
        AbstractSolarPanel self = (AbstractSolarPanel) ((Object) this);
        if(self.hasLevel()){
            Biome b = self.getLevel().getBiomeManager().getBiome(self.getBlockPos()).value();
            boolean rain = b.getPrecipitation() != Biome.Precipitation.NONE;
            float tempEff = 0.3F * (0.8F - b.getBaseTemperature());
            float humidityEff = rain ? -0.3F * b.getDownfall() : 0.0F;
            multiplier = 1.0F + tempEff + humidityEff;
        }
    }

    @Inject(method = "getProduction()Lmekanism/api/math/FloatingLong;", at = @At("HEAD"), cancellable = true,  remap = false)
    private void production(CallbackInfoReturnable<FloatingLong> cir){
        AbstractSolarPanel self = (AbstractSolarPanel) ((Object) this);
        if(self.hasLevel()){
            self.getLevel().getChunkAt(self.getBlockPos()).getCapability(SolarLevelChunk.CAPABILITY)
                    .ifPresent(solar->{
                        if(solar.isActive()){
                            float brightness = 120F;//
                            cir.setReturnValue(getConfiguredMax().multiply(this.multiplier * brightness));
                        }

                    });
        }

    }

    @Inject(method = "getMaxOutput()Lmekanism/api/math/FloatingLong;", at = @At("HEAD"), cancellable = true, remap = false)
    protected void getOutPut(CallbackInfoReturnable<FloatingLong> cir){
        AbstractSolarPanel self = (AbstractSolarPanel) ((Object) this);
        if(self.hasLevel()){
            self.getLevel().getChunkAt(self.getBlockPos()).getCapability(SolarLevelChunk.CAPABILITY)
                    .ifPresent(solar->{
                        if(solar.isActive()){
                            float brightness = 120F;
                                cir.setReturnValue(getConfiguredMax().multiply(this.multiplier * brightness));
                        }
                    });
        }
    }
}
