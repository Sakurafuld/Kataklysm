package com.sakurafuld.kataklysm.content;

import com.sakurafuld.kataklysm.content.anchor.AnchorScreen;
import com.sakurafuld.kataklysm.content.mekaArm.bow.ArrowEntityRenderer;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleAimAdjustmentUnit;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleArrowSpeedUnit;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleBarrageUnit;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleHomingUnit;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.registration.impl.ModuleRegistryObject;
import mekanism.common.registries.MekanismModules;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.kataklysm.Deets.*;

public class Compat {
    public static class Mekanism {
        private static Mekanism INSTANCE;
        private final ResourceKey<Registry<ModuleData<?>>> KEY = ResourceKey.createRegistryKey(identifier(MEKANISM, "module"));
        public static Mekanism get() {
            return INSTANCE == null ? INSTANCE = new Mekanism() : INSTANCE;
        }

        public final ModuleRegistryObject<ModuleArrowSpeedUnit> ARROW_SPEED = moduleRegistryObject("arrow_speed_unit");
        public final ModuleRegistryObject<ModuleBarrageUnit> AIM_ADJUSTMENT = moduleRegistryObject("aim_adjustment_unit");
        public final ModuleRegistryObject<ModuleBarrageUnit> BARRAGE = moduleRegistryObject("barrage_unit");
        public final ModuleRegistryObject<ModuleHomingUnit> HOMING = moduleRegistryObject("homing_unit");

        private <T extends ICustomModule<T>> ModuleRegistryObject<T> moduleRegistryObject(String name) {
            return new ModuleRegistryObject<>(RegistryObject.create(identifier(KATAKLYSM, name), KEY, KATAKLYSM));
        }
        public RegistryObject<Item> registerModuleItem(ModuleRegistryObject<?> provider){
            return ModItems.register("module_" + provider.getInternalRegistryName(),
                    prop -> ModuleHelper.INSTANCE.createModuleItem(provider, prop));
        }

        @SubscribeEvent
        public void registerModule(RegistryEvent.Register<ModuleData<?>> event){
            required(MEKANISM).run(() -> {
                event.getRegistry().register(new ModuleData<>(ModuleData.ModuleDataBuilder.custom(ModuleArrowSpeedUnit::new, () -> ModItems.ARROW_SPEED_UNIT.get())
                        .maxStackSize(4).rarity(Rarity.UNCOMMON).rendersHUD())
                        .setRegistryName(identifier(KATAKLYSM, "arrow_speed_unit")));

                event.getRegistry().register(new ModuleData<>(ModuleData.ModuleDataBuilder.custom(ModuleAimAdjustmentUnit::new, () -> ModItems.AIM_ADJUSTMENT_UNIT.get())
                        .maxStackSize(2).rarity(Rarity.UNCOMMON))
                        .setRegistryName(identifier(KATAKLYSM, "aim_adjustment_unit")));

                event.getRegistry().register(new ModuleData<>(ModuleData.ModuleDataBuilder.custom(ModuleBarrageUnit::new, () -> ModItems.BARRAGE_UNIT.get())
                        .maxStackSize(4).rarity(Rarity.RARE).rendersHUD())
                        .setRegistryName(identifier(KATAKLYSM, "barrage_unit")));

                event.getRegistry().register(new ModuleData<>(ModuleData.ModuleDataBuilder.custom(ModuleHomingUnit::new, () -> ModItems.HOMING_UNIT.get())
                        .maxStackSize(3).rarity(Rarity.RARE))
                        .setRegistryName(identifier(KATAKLYSM, "homing_unit")));

            });
        }
        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public void clientSetup(FMLClientSetupEvent event) {
            required(MEKANISM).run(() -> {
                MenuScreens.register(ModMenus.ANCHOR.get(), AnchorScreen::new);
                ItemProperties.register(ModItems.BOW.get(), identifier("pulling"), ((pStack, pLevel, pEntity, pSeed) ->
                        pEntity instanceof Player && pEntity.isUsingItem() && pEntity.getUseItem().getItem() == pStack.getItem() && pStack.getOrCreateTag().getBoolean("Selected") ? 1 : 0));
                ItemProperties.register(ModItems.BOW.get(), identifier("pull"), ((pStack, pLevel, pEntity, pSeed) ->
                        pEntity instanceof Player && pEntity.isUsingItem() && pEntity.getUseItem().getItem() == pStack.getItem() && pStack.getOrCreateTag().getBoolean("Selected") ? (pStack.getUseDuration() - pEntity.getUseItemRemainingTicks()) / 20f : 0));
                ItemProperties.register(ModItems.BOW.get(), identifier("melee"), ((pStack, pLevel, pEntity, pSeed) -> {
                    IModule<ModuleAttackAmplificationUnit> module = MekanismAPI.getModuleHelper().load(pStack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
                    return module != null && module.isEnabled() ? 1 : 0;
                }));
                EntityRenderers.register(ModEntities.ARROW.get(), ArrowEntityRenderer::new);
            });
        }
    }
}
