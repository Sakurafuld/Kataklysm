package com.sakurafuld.kataklysm.content;

import com.sakurafuld.kataklysm.Deets;
import com.sakurafuld.kataklysm.content.anchor.AnchorBlock;
import com.sakurafuld.kataklysm.content.oneness.OnenessBlock;
import com.sakurafuld.kataklysm.content.solar.SolarBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.sakurafuld.kataklysm.Deets.*;

public class ModBlocks {
    public static final DeferredRegister<Block> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCKS, KATAKLYSM);

//    public static final RegistryObject<Block> WELL;
    public static final RegistryObject<Block> ANCHOR;
    public static final RegistryObject<Block> SOLAR;
    public static final RegistryObject<Block> ONENESS;
    public static final RegistryObject<Block> POT;

    static {

//        WELL = required(MEKANISM).get(()-> register("well", ()-> new WellBlock(BlockBehaviour.Properties.of(Material.METAL).strength(3f, 4.8f).noOcclusion().requiresCorrectToolForDrops())));
        ANCHOR = required(MEKANISM).getOr(()-> register("anchor", ()-> new AnchorBlock(BlockBehaviour.Properties.of(Material.METAL).strength(5.0f, 9.0f).noOcclusion().requiresCorrectToolForDrops())),
                () -> dummy("anchor"));

        SOLAR = required(BOTANIA).getOr(()-> register("solar", ()-> new SolarBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(3f, 10f).noOcclusion().requiresCorrectToolForDrops())),
                () -> dummy("solar"));

//        ONENESS = required(DRACONICEVOLUTION).get(()-> register("oneness", ()-> new OnenessBlock(BlockBehaviour.Properties.of(Material.METAL).strength(20.0F, 600.0F).noOcclusion().requiresCorrectToolForDrops())));

        ONENESS = required(DRACONICEVOLUTION).getOr(() -> register("oneness", () -> new OnenessBlock(BlockBehaviour.Properties.of(Material.METAL).strength(20.0F, 600.0F).noOcclusion().requiresCorrectToolForDrops())),
                () -> dummy("oneness"));

        POT = null;//requiredAll(BLOODMAGIC, BOTANYPOTS).get(()-> register("pot", ()-> new PotBlock(BlockBehaviour.Properties.of(Material.STONE).strength(2,4.2f).noOcclusion().lightLevel(state-> state.getValue(BlockStateProperties.LEVEL)))));

    }

    public static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = REGISTRY.register(name, block);
        ModItems.REGISTRY.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties().tab(Deets.TAB)));

        return toReturn;
    }
    public static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block, BiFunction<T, Item.Properties, ? extends BlockItem> item){
        RegistryObject<T> toReturn = REGISTRY.register(name, block);
        ModItems.REGISTRY.register(name, () -> item.apply(toReturn.get(), new Item.Properties().tab(Deets.TAB)));
        return toReturn;
    }
    //タグ定義のバグ防止.
    public static RegistryObject<Block> dummy(String id) {
        return REGISTRY.register(id, () -> new Block(BlockBehaviour.Properties.of(Material.AIR)));
    }
}
