package com.sakurafuld.kataklysm.content.mekaArm.bow.modules;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.network.chat.Component;

public class ModuleAimAdjustmentUnit implements ICustomModule<ModuleAimAdjustmentUnit> {
    private IModuleConfigItem<AimAdjustment> aimAdjustment;

    @Override
    public void init(IModule<ModuleAimAdjustmentUnit> module, ModuleConfigItemCreator configItemCreator) {
        this.aimAdjustment = configItemCreator.createConfigItem("aim_adjustment", () -> "module.kataklysm.aim_adjustment",
                new ModuleEnumData<>(AimAdjustment.class, module.getInstalledCount() + 2, AimAdjustment.NORMAL));
    }
    public float getAdjustment() {
        return this.aimAdjustment.get().getAdjustment();
    }

    public enum AimAdjustment implements IHasTextComponent {
        STORMTROOPER(3),
        NORMAL(1),
        SNIPER(0.5f),
        HAWKEYE(0);

        private final float adjustment;
        private final Component label;

        AimAdjustment(float adjustment) {
            this.adjustment = adjustment;
            this.label = TextComponentUtil.getString(Float.toString(adjustment));
        }
        AimAdjustment(int adjustment) {
            this.adjustment = adjustment;
            this.label = TextComponentUtil.getString(Integer.toString(adjustment));
        }

        @Override
        public Component getTextComponent() {
            return this.label;
        }
        public float getAdjustment() {
            return this.adjustment;
        }
    }
}
