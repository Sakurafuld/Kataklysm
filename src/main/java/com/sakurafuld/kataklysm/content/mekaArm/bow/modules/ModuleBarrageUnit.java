package com.sakurafuld.kataklysm.content.mekaArm.bow.modules;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class ModuleBarrageUnit implements ICustomModule<ModuleBarrageUnit> {
    private IModuleConfigItem<Barrage> barrage;

    @Override
    public void init(IModule<ModuleBarrageUnit> module, ModuleConfigItemCreator configItemCreator) {
        this.barrage = configItemCreator.createConfigItem("barrage", () -> "module.kataklysm.barrage",
                new ModuleEnumData<>(Barrage.class, module.getInstalledCount() + 1, Barrage.SOLO));
    }
    public int getCount() {
        return this.barrage.get().ordinal();
    }
    @Override
    public void addHUDStrings(IModule<ModuleBarrageUnit> module, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(new TranslatableComponent("module.kataklysm.barrage_overlay").withStyle(style -> style.withColor(EnumColor.DARK_GRAY.getColor()))
                    .append(new TextComponent(" " + this.barrage.get().getTextComponent().getString()).withStyle(style -> style.withColor(EnumColor.INDIGO.getColor()))));
        }
    }
    public enum Barrage implements IHasTextComponent {
        SOLO,
        DUO,
        TRIO,
        QUARTET,
        QUINTET;

        private final Component label;

        Barrage() {
            this.label = TextComponentUtil.getString(Integer.toString(this.ordinal()));
        }

        @Override
        public Component getTextComponent() {
            return this.label;
        }
    }
}
