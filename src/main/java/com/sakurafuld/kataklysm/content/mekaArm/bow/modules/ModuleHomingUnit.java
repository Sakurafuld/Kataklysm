package com.sakurafuld.kataklysm.content.mekaArm.bow.modules;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.network.chat.Component;

public class ModuleHomingUnit implements ICustomModule<ModuleHomingUnit> {
    private IModuleConfigItem<Homing> homing;

    @Override
    public void init(IModule<ModuleHomingUnit> module, ModuleConfigItemCreator configItemCreator) {
        this.homing = configItemCreator.createConfigItem("homing", () -> "module.kataklysm.homing",
                new ModuleEnumData<>(Homing.class, module.getInstalledCount() + 1, Homing.LOW));
    }
    public int getHoming() {
        return this.homing.get().getHoming();
    }

    public enum Homing implements IHasTextComponent {
        NONE(-1, "None"),
        LOW(20, "Low"),
        MEDIUM(10, "Medium"),
        HIGH(3, "High");

        private final int homing;
        private final Component label;

        Homing(int homing, String name){
            this.homing = homing;
            this.label = TextComponentUtil.getString(name);
        }

        @Override
        public Component getTextComponent() {
            return this.label;
        }
        public int getHoming() {
            return this.homing;
        }
    }
}
