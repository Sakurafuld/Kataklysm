package com.sakurafuld.kataklysm.common.init;

import com.sakurafuld.kataklysm.Kataklysm;
import com.sakurafuld.kataklysm.common.block.anchor.AnchorBlock;
import com.sakurafuld.kataklysm.common.block.solar.SolarBlock;
import com.sakurafuld.kataklysm.common.block.well.WellBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Kataklysm.ID);


    public static final RegistryObject<Block> WELL;

    public static final RegistryObject<Block> ANCHOR;

    public static final RegistryObject<Block> SOLAR;

    static{
        if (ModList.get().isLoaded("mekanism")){
            WELL = register("well", () -> new WellBlock(BlockBehaviour.Properties.of(Material.METAL).noOcclusion().strength(3f, 4.8f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
            ANCHOR = register("anchor", () -> new AnchorBlock(BlockBehaviour.Properties.of(Material.METAL).noOcclusion().strength(5.0f, 9.0f).requiresCorrectToolForDrops()));
        }else {
            WELL = null;
            ANCHOR = null;
        }
        if(ModList.get().isLoaded("botania")){
            SOLAR = register("solar", () -> new SolarBlock(BlockBehaviour.Properties.of(Material.GLASS).noOcclusion().strength(3f, 10f)));
        }else {
            SOLAR = null;
        }
    }

    public static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(),
                new Item.Properties().tab(Kataklysm.TAB)));

        return toReturn;
    }
    public static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block, BiFunction<T, Item.Properties, ? extends BlockItem> item){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> item.apply(toReturn.get(), new Item.Properties().tab(Kataklysm.TAB)));
        return toReturn;
    }
}
