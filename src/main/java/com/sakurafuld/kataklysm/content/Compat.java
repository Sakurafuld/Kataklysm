package com.sakurafuld.kataklysm.content;

import com.sakurafuld.kataklysm.content.anchor.AnchorScreen;
import com.sakurafuld.kataklysm.content.mekaArm.bow.ArrowEntityRenderer;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleAimAdjustmentUnit;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleArrowSpeedUnit;
import com.sakurafuld.kataklysm.content.mekaArm.bow.modules.ModuleBarrageUnit;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.mekatool.ModuleAttackAmplificationUnit;
import mekanism.common.registration.impl.ModuleRegistryObject;
import mekanism.common.registries.MekanismModules;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.kataklysm.Deets.*;

public class Compat {
    public static class Mekanism {
        private static Mekanism INSTANCE;
        public static Mekanism get(){
            return INSTANCE == null ? INSTANCE = new Mekanism() : INSTANCE;
        }
        public final ModuleRegistryObject<ModuleArrowSpeedUnit> ARROW_SPEED =
                new ModuleRegistryObject<>(RegistryObject.create(identifier(KATAKLYSM, "arrow_speed_unit"), ResourceKey.createRegistryKey(new ResourceLocation(MEKANISM, "module")), KATAKLYSM));
        public final ModuleRegistryObject<ModuleBarrageUnit> BARRAGE =
                new ModuleRegistryObject<>(RegistryObject.create(identifier(KATAKLYSM, "barrage_unit"), ResourceKey.createRegistryKey(new ResourceLocation(MEKANISM, "module")), KATAKLYSM));
        public final ModuleRegistryObject<ModuleBarrageUnit> AIM_ADJUSTMENT =
                new ModuleRegistryObject<>(RegistryObject.create(identifier(KATAKLYSM, "aim_adjustment_unit"), ResourceKey.createRegistryKey(new ResourceLocation(MEKANISM, "module")), KATAKLYSM));


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

//                Compat.Mekanism.ARROW_SPEED.updateReference(event.getRegistry());
            });
        }
        @SubscribeEvent
        public void clientSetup(FMLClientSetupEvent event) {
            required(MEKANISM).run(() -> {
                MenuScreens.register(ModMenus.ANCHOR.get(), AnchorScreen::new);
                ItemProperties.register(ModItems.BOW.get(), identifier("pulling"), ((pStack, pLevel, pEntity, pSeed) -> {

                    return pEntity instanceof Player && pEntity.isUsingItem() && pEntity.getUseItem().getItem() == pStack.getItem() && pStack.getOrCreateTag().getBoolean("Selected") ? 1 : 0;
                }));
                ItemProperties.register(ModItems.BOW.get(), identifier("pull"), ((pStack, pLevel, pEntity, pSeed) -> {
//                if(player.isShiftKeyDown())
//                    LOG.debug("{}-Enull={}-Uing={}-Eq={}-Dura={}-Remai={}", side(), pEntity != null, pEntity.isUsingItem(), !pEntity.getUseItem().getItem().shouldCauseReequipAnimation(pEntity.getUseItem(), pStack, false), pStack.getUseDuration(), pEntity.getUseItemRemainingTicks());
                    return pEntity instanceof Player && pEntity.isUsingItem() && pEntity.getUseItem().getItem() == pStack.getItem() && pStack.getOrCreateTag().getBoolean("Selected") ? (pStack.getUseDuration() - pEntity.getUseItemRemainingTicks()) / 20f : 0;
                }));
                ItemProperties.register(ModItems.BOW.get(), identifier("melee"), ((pStack, pLevel, pEntity, pSeed) -> {
                    IModule<ModuleAttackAmplificationUnit> module = MekanismAPI.getModuleHelper().load(pStack, MekanismModules.ATTACK_AMPLIFICATION_UNIT);
                    return module != null && module.isEnabled() ? 1 : 0;
                }));
                EntityRenderers.register(ModEntities.ARROW.get(), ArrowEntityRenderer::new);
            });
        }
        @SubscribeEvent
        public void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
//            required(MEKANISM).run(() -> {
//                event.registerBlockEntityRenderer(ModBlockEntities.ANCHOR.get(), AnchorBlockEntityRenderer::new);
//            });
        }
    }
}
