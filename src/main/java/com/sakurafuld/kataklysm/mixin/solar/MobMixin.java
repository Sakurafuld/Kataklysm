package com.sakurafuld.kataklysm.mixin.solar;

import com.sakurafuld.kataklysm.common.capability.SolarChunkProvider;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "isSunBurnTick()Z", at = @At("HEAD"), cancellable = true)
    private void isBurnable(CallbackInfoReturnable<Boolean> cir){
        Mob self = (Mob) ((Object) this);
        self.getLevel().getChunkAt(self.blockPosition()).getCapability(SolarChunkProvider.SOLAR)
                .ifPresent(solar->{
                    if(solar.isActive()) cir.setReturnValue(true);
                });
    }
}
