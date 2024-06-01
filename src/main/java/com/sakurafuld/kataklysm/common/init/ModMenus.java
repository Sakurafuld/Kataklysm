package com.sakurafuld.kataklysm.common.init;

import com.sakurafuld.kataklysm.Kataklysm;
import com.sakurafuld.kataklysm.client.gui.anchor.AnchorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, Kataklysm.ID);
    public static final RegistryObject<MenuType<AnchorMenu>> ANCHOR =
            MENUS.register("anchor", ()-> IForgeMenuType.create(AnchorMenu::new));
}
