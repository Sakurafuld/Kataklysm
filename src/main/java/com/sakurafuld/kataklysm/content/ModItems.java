package com.sakurafuld.kataklysm.content;

import com.sakurafuld.kataklysm.content.mekaArm.bow.BowItem;
import com.sakurafuld.kataklysm.content.oneness.OnenessTouchItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

import static com.sakurafuld.kataklysm.Deets.*;

public class ModItems {
    public static final DeferredRegister<Item> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ITEMS, KATAKLYSM);


    public static final RegistryObject<Item> ONENESS_TOUCH;

    public static final RegistryObject<Item> BOW;


    public static final RegistryObject<Item> ARROW_SPEED_UNIT;
    public static final RegistryObject<Item> AIM_ADJUSTMENT_UNIT;
    public static final RegistryObject<Item> BARRAGE_UNIT;

    static {

        BOW = required(MEKANISM).get(() -> register("bow", BowItem::new));
        ARROW_SPEED_UNIT = required(MEKANISM).get(() -> Compat.Mekanism.get().registerModuleItem(Compat.Mekanism.get().ARROW_SPEED));
        AIM_ADJUSTMENT_UNIT = required(MEKANISM).get(() -> Compat.Mekanism.get().registerModuleItem(Compat.Mekanism.get().AIM_ADJUSTMENT));
        BARRAGE_UNIT = required(MEKANISM).get(() -> Compat.Mekanism.get().registerModuleItem(Compat.Mekanism.get().BARRAGE));


        ONENESS_TOUCH = register("oneness_touch", OnenessTouchItem::new);

    }
    public static RegistryObject<Item> register(String name){
        return register(name, new Item.Properties().tab(TAB));
    }
    public static RegistryObject<Item> register(String name, Item.Properties prop){
        return REGISTRY.register(name, ()-> new Item(prop));
    }
    public static RegistryObject<Item> register(String name, Function<Item.Properties, ? extends Item> func){
        return REGISTRY.register(name, ()-> func.apply(new Item.Properties().tab(TAB)));
    }
}
