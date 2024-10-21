package com.sakurafuld.kataklysm;

import com.sakurafuld.kataklysm.api.event.SpecialKeyEvent;
import com.sakurafuld.kataklysm.content.*;
import com.sakurafuld.kataklysm.content.anchor.AnchorOverlay;
import com.sakurafuld.kataklysm.content.oneness.OnenessBlockEntityRenderer;
import com.sakurafuld.kataklysm.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.sakurafuld.kataklysm.Deets.*;

@Mod(KATAKLYSM)
public class Kataklysm {
    public Kataklysm() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::registerBlockEntityRenderers);

        ModBlockEntities.REGISTRY.register(bus);
        ModBlocks.REGISTRY.register(bus);
        ModEntities.REGISTRY.register(bus);
        ModItems.REGISTRY.register(bus);
        ModMenus.REGISTRY.register(bus);
        ModParticles.REGISTRY.register(bus);
        bus.addListener(ModParticles::register);
        ModSounds.REGISTRY.register(bus);

        required(MEKANISM).run(() -> bus.register(new Compat.Mekanism()));

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "kataklysm.toml");
        //コミットできてるかな？.
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::initialize);
    }
    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            new SpecialKeyEvent().setup(Minecraft.getInstance());

            required(BOTANIA).run(() -> {
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.SOLAR.get(), RenderType.translucent());
            });
            required(MEKANISM).run(() -> {
                OverlayRegistry.registerOverlayTop("Anchor", new AnchorOverlay());
            });
        });
    }
    public void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        required(DRACONICEVOLUTION).run(() -> {
            event.registerBlockEntityRenderer(ModBlockEntities.ONENESS.get(), OnenessBlockEntityRenderer::new);

        });

//        requiredAll(BOTANYPOTS, BLOODMAGIC).run(() -> {
//            event.registerBlockEntityRenderer(ModBlockEntities.POT.get(), BotanyPotRenderer::new);
//        });
    }
}
