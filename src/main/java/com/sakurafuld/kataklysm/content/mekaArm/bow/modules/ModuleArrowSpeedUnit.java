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

public class ModuleArrowSpeedUnit implements ICustomModule<ModuleArrowSpeedUnit> {
    private IModuleConfigItem<ArrowSpeed> arrowSpeed;

    @Override
    public void init(IModule<ModuleArrowSpeedUnit> module, ModuleConfigItemCreator configItemCreator) {
        this.arrowSpeed = configItemCreator.createConfigItem("arrow_speed", () -> "module.kataklysm.arrow_speed",
                new ModuleEnumData<>(ArrowSpeed.class, module.getInstalledCount() + 2, ArrowSpeed.NORMAL));
    }
    public float getSpeed(){
        return this.arrowSpeed.get().getSpeed();
    }
    public float getInaccuracy(){
        return this.arrowSpeed.get().getInaccuracy();
    }
    public float getEnergyMultiplier(){
        return this.arrowSpeed.get().getEnergyMultiplier();
    }
    
    @Override
    public void addHUDStrings(IModule<ModuleArrowSpeedUnit> module, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(new TranslatableComponent("module.kataklysm.arrow_speed_overlay").withStyle(style -> style.withColor(EnumColor.DARK_GRAY.getColor()))
                    .append(new TextComponent(" " + this.arrowSpeed.get().getTextComponent().getString()).withStyle(style -> style.withColor(EnumColor.INDIGO.getColor()))));
        }
    }

    public enum ArrowSpeed implements IHasTextComponent {
        SLOW(0.5f, 0, 0.25f),
        NORMAL(1, 1, 1),
        FAST(1.5f, 2, 1.25f),
        QUICK(3, 4, 5),
        MACH(6, 8, 20),
        LIGHTSPEED(8, 16, 40);

        private final float speed;
        private final float inaccuracy;
        private final float energyMultiplier;
        private final Component label;
        ArrowSpeed(float speed, float inaccuracy, float energyMultiplier){
            this.speed = speed;
            this.inaccuracy = inaccuracy;
            this.energyMultiplier = energyMultiplier;
            this.label = TextComponentUtil.getString(Float.toString(speed));
        }
        ArrowSpeed(int speed, float inaccuracy, float energyMultiplier){
            this.speed = speed;
            this.inaccuracy = inaccuracy;
            this.energyMultiplier = energyMultiplier;
            this.label = TextComponentUtil.getString(Integer.toString(speed));
        }

        @Override
        public Component getTextComponent() {
            return this.label;
        }
        public float getSpeed() {
            return this.speed;
        }
        public float getInaccuracy() {
            return this.inaccuracy;
        }
        public float getEnergyMultiplier() {
            return this.energyMultiplier;
        }
    }
}
