package com.sakurafuld.kataklysm;

import com.sakurafuld.kataklysm.client.gui.anchor.AnchorScreen;
import com.sakurafuld.kataklysm.client.render.block.AnchorBlockEntityRenderer;
import com.sakurafuld.kataklysm.common.capability.SolarChunk;
import com.sakurafuld.kataklysm.common.init.ModBlockEntities;
import com.sakurafuld.kataklysm.common.init.ModBlocks;
import com.sakurafuld.kataklysm.common.init.ModItems;
import com.sakurafuld.kataklysm.common.init.ModMenus;
import com.sakurafuld.kataklysm.common.network.PacketHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Kataklysm.ID)
public class Kataklysm {
    public static final String ID = "kataklysm";
    public static final CreativeModeTab TAB =
            new CreativeModeTab("kataklysm"){

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.DRAGON_HEAD);
        }
    };

    public Kataklysm(){
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::registerBlockEntityRenderer);
        bus.addListener(this::onRegisterCapability);
        ModItems.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModMenus.MENUS.register(bus);
    }
    public void commonSetup(FMLCommonSetupEvent event){
        event.enqueueWork(PacketHandler::init);
    }
    public void clientSetup(FMLClientSetupEvent event){
        if(ModList.get().isLoaded("mekanism")){
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WELL.get(), RenderType.translucent());
            MenuScreens.register(ModMenus.ANCHOR.get(), AnchorScreen::new);
        }
        if(ModList.get().isLoaded("botania")){
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SOLAR.get(), RenderType.translucent());
        }

    }
    public void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers event){
        if(ModList.get().isLoaded("mekanism")){
            event.registerBlockEntityRenderer(ModBlockEntities.ANCHOR.get(), AnchorBlockEntityRenderer::new);
        }
    }
    public void onRegisterCapability(RegisterCapabilitiesEvent event){
        event.register(SolarChunk.class);
    }
}