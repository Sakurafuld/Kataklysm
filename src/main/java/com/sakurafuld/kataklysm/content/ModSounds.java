package com.sakurafuld.kataklysm.content;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.kataklysm.Deets.*;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY
            = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, KATAKLYSM);

    public static final RegistryObject<SoundEvent> CHARGE_SHOOT;
    public static final RegistryObject<SoundEvent> VANISH;

    static {

        CHARGE_SHOOT = REGISTRY.register("charge_shoot", ()-> new SoundEvent(identifier(KATAKLYSM, "charge_shoot")));
        VANISH = REGISTRY.register("vanish", ()-> new SoundEvent(identifier(KATAKLYSM, "vanish")));

    }
}
