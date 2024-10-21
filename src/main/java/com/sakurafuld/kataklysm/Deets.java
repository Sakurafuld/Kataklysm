package com.sakurafuld.kataklysm;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Supplier;

public class Deets {
    private Deets(){}

    public static final String KATAKLYSM = "kataklysm";
    public static final Logger LOG = LoggerFactory.getLogger(KATAKLYSM);
    public static final CreativeModeTab TAB = new CreativeModeTab(KATAKLYSM){@Override public ItemStack makeIcon() {return new ItemStack(Items.DRAGON_HEAD);}};

    public static final String CURIOS = "curios";
    public static final String MEKANISM = "mekanism";
    public static final String BOTANIA = "botania";
    public static final String DRACONICEVOLUTION = "draconicevolution";

    public static Act required(String modid) {
        return FMLLoader.getLoadingModList().getModFileById(modid) != null ? Act.TRUE : Act.FALSE;
    }
    public static Act requiredAll(String... modids){
        return Arrays.stream(modids).allMatch(modid->FMLLoader.getLoadingModList().getModFileById(modid) != null) ? Act.TRUE : Act.FALSE;
    }

    public static ResourceLocation identifier(String path){
        return new ResourceLocation(path);
    }
    public static ResourceLocation identifier(String nameSpace, String path){
        return new ResourceLocation(nameSpace, path);
    }
    public static ResourceLocation[] identifier(String nameSpace, String... paths){
        ResourceLocation[] resourceLocations = new ResourceLocation[paths.length];
        for(int index = 0; index < paths.length; index++){
            resourceLocations[index] = new ResourceLocation(nameSpace, paths[index]);
        }
        return resourceLocations;
    }
    public static Act required(ResourceLocation key){
        return ForgeRegistries.ITEMS.containsKey(key) ? Act.TRUE : Act.FALSE;
    }
    public static Act requiredAny(ResourceLocation... keys){
        return Arrays.stream(keys).anyMatch(ForgeRegistries.ITEMS::containsKey) ? Act.TRUE : Act.FALSE;
    }
    public static Act requiredAll(ResourceLocation... keys){
        return Arrays.stream(keys).allMatch(ForgeRegistries.ITEMS::containsKey) ? Act.TRUE : Act.FALSE;
    }
    public static LogicalSide side(){
        return EffectiveSide.get();
    }
    public static Act required(LogicalSide side){
        return side() == side ? Act.TRUE : Act.FALSE;
    }

    public enum Act {
        FALSE,
        TRUE;

        public void run(Runnable runnable){
            switch (this){
                case FALSE -> {return;}
                case TRUE -> {runnable.run();return;}
            }
            throw new IllegalStateException();
        }
        public void runOr(Runnable trueRun, Runnable falseRun){
            switch (this){
                case FALSE -> {falseRun.run();return;}
                case TRUE -> {trueRun.run();return;}
            }
            throw new IllegalStateException();
        }
        public <T> T get(Supplier<T> supplier){
            switch (this){
                case FALSE -> {return null;}
                case TRUE -> {return supplier.get();}
            }
            throw new IllegalStateException();
        }
        public <T> T getOr(Supplier<T> trueGet, Supplier<T> falseGet){
            switch (this){
                case FALSE -> {return falseGet.get();}
                case TRUE -> {return trueGet.get();}
            }
            throw new IllegalStateException();
        }
        public boolean ready(){
            switch (this){
                case FALSE -> {return false;}
                case TRUE -> {return true;}
            }
            throw new IllegalStateException();
        }
    }
    public static class Config {
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.IntValue ANCHOR_DISTANCE;
        public static final ForgeConfigSpec.IntValue SOLAR_MAX;
        public static final ForgeConfigSpec.IntValue SOLAR_RATE;

        static {
            ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

            BUILDER.push("Blocks");

            ANCHOR_DISTANCE = BUILDER
                    .comment("Maximum distance that can be teleported using the Discorder and Meka-Tool",
                            "Render distance must be set higher than this in the settings to work",
                            "Default: 128")
                    .defineInRange("Discorder Distance", 128, 0, Integer.MAX_VALUE);
            SOLAR_MAX = BUILDER
                    .comment("Maximum storage of mana in the Miniature Sunshine",
                            "Default: 100000")
                    .defineInRange("Miniature Sunshine Max Mana", 100000, 0, Integer.MAX_VALUE);
            SOLAR_RATE = BUILDER
                    .comment("Miniature Sunshine mana consumption rates",
                            "Default: 800")
                    .defineInRange("Miniature Sunshine Rate", 800, 0, Integer.MAX_VALUE);

            BUILDER.pop();

            SPEC = BUILDER.build();
        }
    }
}