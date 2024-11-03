package com.sakurafuld.kataklysm.content.mekaArm.bow.modules;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.network.chat.Component;

public class ModuleHomingUnit implements ICustomModule<ModuleHomingUnit> {
    private IModuleConfigItem<Frequency> frequency;
    private IModuleConfigItem<Distance> distance;
    private IModuleConfigItem<Boolean> back;

    @Override
    public void init(IModule<ModuleHomingUnit> module, ModuleConfigItemCreator configItemCreator) {
        this.frequency = configItemCreator.createConfigItem("homing_frequency", () -> "module.kataklysm.homing_frequency",
                new ModuleEnumData<>(Frequency.class, module.getInstalledCount() + 1, Frequency.LOW));
        this.distance = configItemCreator.createConfigItem("homing_distance", () -> "module.kataklysm.homing_distance",
                new ModuleEnumData<>(Distance.class, module.getInstalledCount() + 1, Distance.LOW));
        this.back = configItemCreator.createConfigItem("homing_back", () -> "module.kataklysm.homing_back",
                new ModuleBooleanData(false));
    }
    public int getFrequency() {
        return this.frequency.get().getFrequency();
    }
    public int getDistance() {
        return this.distance.get().getDistance();
    }
    public boolean isBack() {
        return this.back.get();
    }

    public enum Frequency implements IHasTextComponent {
        NONE(-1, "None"),
        LOW(40, "Low"),
        MEDIUM(10, "Medium"),
        HIGH(3, "High");

        private final int frequency;
        private final Component label;

        Frequency(int frequency, String name){
            this.frequency = frequency;
            this.label = TextComponentUtil.getString(name);
        }

        @Override
        public Component getTextComponent() {
            return this.label;
        }
        public int getFrequency() {
            return this.frequency;
        }
    }
    public enum Distance implements IHasTextComponent {
        NONE(0),
        LOW(15),
        MEDIUM(50),
        HIGH(100);

        private final int distance;
        private final Component label;

        Distance(int distance){
            this.distance = distance;
            this.label = TextComponentUtil.getString(Integer.toString(distance));
        }

        @Override
        public Component getTextComponent() {
            return this.label;
        }
        public int getDistance() {
            return this.distance;
        }
    }
}
