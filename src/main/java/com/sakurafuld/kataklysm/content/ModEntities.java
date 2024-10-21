package com.sakurafuld.kataklysm.content;

import com.sakurafuld.kataklysm.content.mekaArm.bow.ArrowEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.kataklysm.Deets.*;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENTITIES, KATAKLYSM);


    public static final RegistryObject<EntityType<ArrowEntity>> ARROW;

    static {

        ARROW = REGISTRY.register("arrow",
                () -> EntityType.Builder.of((EntityType.EntityFactory<ArrowEntity>) ArrowEntity::new, MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(10).build("arrow"));

    }


}
