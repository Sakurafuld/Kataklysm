package com.sakurafuld.kataklysm.content;

import com.sakurafuld.kataklysm.content.anchor.AnchorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.kataklysm.Deets.*;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, KATAKLYSM);

    public static final RegistryObject<MenuType<AnchorMenu>> ANCHOR;

    static {

        ANCHOR = REGISTRY.register("anchor", ()-> IForgeMenuType.create(AnchorMenu::new));

    }
}
