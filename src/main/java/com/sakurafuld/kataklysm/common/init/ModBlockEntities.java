package com.sakurafuld.kataklysm.common.init;

import com.sakurafuld.kataklysm.Kataklysm;
import com.sakurafuld.kataklysm.common.block.anchor.AnchorBlockEntity;
import com.sakurafuld.kataklysm.common.block.solar.SolarBlockEntity;
import com.sakurafuld.kataklysm.common.block.well.WellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Kataklysm.ID);

    public static final RegistryObject<BlockEntityType<WellBlockEntity>> WELL;

    public static final RegistryObject<BlockEntityType<AnchorBlockEntity>> ANCHOR;

    public static final RegistryObject<BlockEntityType<SolarBlockEntity>> SOLAR;
    static{
        if (ModList.get().isLoaded("mekanism")){
            WELL = BLOCK_ENTITIES.register("well", ()-> BlockEntityType.Builder.of(WellBlockEntity::new, ModBlocks.WELL.get()).build(null));
            ANCHOR = BLOCK_ENTITIES.register("anchor", ()-> BlockEntityType.Builder.of(AnchorBlockEntity::new, ModBlocks.ANCHOR.get()).build(null));

        }else {
            WELL = null;
            ANCHOR = null;
        }
        if (ModList.get().isLoaded("botania")) {
            SOLAR = BLOCK_ENTITIES.register("solar", ()-> BlockEntityType.Builder.of(SolarBlockEntity::new, ModBlocks.SOLAR.get()).build(null));
        }else{
            SOLAR = null;
        }
    }
}
