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
    public float getHoming() {
        return this.homing.get().getHoming();
    }

    public enum Homing implements IHasTextComponent {
        NONE(0, "None"),
        LOW(1, "Low"),
        MEDIUM(2, "Medium"),
        HIGH(3, "High");

        private final float homing;
        private final Component label;

        Homing(int homing, String name){
            this.homing = homing;
            this.label = TextComponentUtil.getString(name);
        }

        @Override
        public Component getTextComponent() {
            return this.label;
        }
        public float getHoming() {
            return this.homing;
        }
    }
}
