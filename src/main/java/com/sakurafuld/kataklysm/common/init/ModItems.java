package com.sakurafuld.kataklysm.common.init;

import com.sakurafuld.kataklysm.Kataklysm;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Kataklysm.ID);

/*public static final RegistryObject<Item> TEST =
        register("test");*/
    public static RegistryObject<Item> register(String name){
        return register(name, new Item.Properties().tab(Kataklysm.TAB));
    }
    public static RegistryObject<Item> register(String name, Item.Properties prop){
        return ITEMS.register(name, ()-> new Item(prop));
    }
    public static RegistryObject<Item> register(String name, Function<Item.Properties, ? extends Item> func){
        return ITEMS.register(name, ()-> func.apply(new Item.Properties().tab(Kataklysm.TAB)));
    }

}
