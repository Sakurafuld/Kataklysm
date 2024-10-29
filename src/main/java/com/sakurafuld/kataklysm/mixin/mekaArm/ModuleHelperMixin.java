package com.sakurafuld.kataklysm.mixin.mekaArm;

import com.google.common.collect.ImmutableSet;
import com.sakurafuld.kataklysm.content.Compat;
import com.sakurafuld.kataklysm.content.ModItems;
import mekanism.api.gear.ModuleData;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.registries.MekanismModules;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.sakurafuld.kataklysm.Deets.LOG;
import static com.sakurafuld.kataklysm.Deets.side;

@Pseudo
@Mixin(value = ModuleHelper.class, remap = false)
public abstract class ModuleHelperMixin {
    @Shadow(remap = false) @Final private Map<Item, Set<ModuleData<?>>> supportedModules;
    @Shadow(remap = false) @Final private Map<ModuleData<?>, Set<Item>> supportedContainers;


    @Inject(method = "processIMC", at = @At("TAIL"), remap = false)
    private void processIMCMekaArm(CallbackInfo ci){
        LOG.info("{}-processIMCMixin", side());
        Set<ModuleData<?>> bowModules = new HashSet<>();

        bowModules.add(MekanismModules.ENERGY_UNIT.getModuleData());
        bowModules.add(MekanismModules.ATTACK_AMPLIFICATION_UNIT.getModuleData());
        bowModules.add(Compat.Mekanism.get().ARROW_SPEED.get());
        bowModules.add(Compat.Mekanism.get().AIM_ADJUSTMENT.get());
        bowModules.add(Compat.Mekanism.get().BARRAGE.get());
        bowModules.add(Compat.Mekanism.get().HOMING.get());

        this.supportedModules.put(ModItems.BOW.get(), bowModules);

        for(ModuleData<?> module : bowModules) {
            //パワーでやっちゃう.
            ImmutableSet.Builder<Item> review = ImmutableSet.builder();
            review.addAll(this.supportedContainers.getOrDefault(module, new HashSet<>()));
            review.add(ModItems.BOW.get());
            this.supportedContainers.put(module, review.build());
        }
    }
}
