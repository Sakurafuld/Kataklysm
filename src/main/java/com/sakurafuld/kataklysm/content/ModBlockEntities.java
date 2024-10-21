package com.sakurafuld.kataklysm.content;

import com.sakurafuld.kataklysm.content.anchor.AnchorBlockEntity;
import com.sakurafuld.kataklysm.content.oneness.OnenessBlockEntity;
import com.sakurafuld.kataklysm.content.solar.SolarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.kataklysm.Deets.*;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, KATAKLYSM);

//    public static final RegistryObject<BlockEntityType<WellBlockEntity>> WELL;
    public static final RegistryObject<BlockEntityType<AnchorBlockEntity>> ANCHOR;
    public static final RegistryObject<BlockEntityType<SolarBlockEntity>> SOLAR;
    public static final RegistryObject<BlockEntityType<OnenessBlockEntity>> ONENESS;

    public static final RegistryObject<BlockEntityType<BlockEntity>> POT;

    static {

//        WELL = required(MEKANISM).get(()-> REGISTRY.register("well", ()-> BlockEntityType.Builder.of(WellBlockEntity::new, ModBlocks.WELL.get()).build(null)));
        ANCHOR = required(MEKANISM).get(()-> REGISTRY.register("anchor", ()-> BlockEntityType.Builder.of(AnchorBlockEntity::new, ModBlocks.ANCHOR.get()).build(null)));

        SOLAR = required(BOTANIA).get(()-> REGISTRY.register("solar", ()-> BlockEntityType.Builder.of(SolarBlockEntity::new, ModBlocks.SOLAR.get()).build(null)));

//        ONENESS = required(DRACONICEVOLUTION).get(()-> REGISTRY.register("oneness", ()-> BlockEntityType.Builder.of(OnenessBlockEntity::new, ModBlocks.ONENESS.get()).build(null)));

        ONENESS = REGISTRY.register("oneness", () -> BlockEntityType.Builder.of(OnenessBlockEntity::new, ModBlocks.ONENESS.get()).build(null));

        POT = null;//requiredAll(BOTANYPOTS, BLOODMAGIC).get(()-> REGISTRY.register("pot", ()-> BlockEntityType.Builder.of(PotBlockEntity::new, ModBlocks.POT.get()).build(null)));
    }
}
