package com.sakurafuld.kataklysm.content;

import com.sakurafuld.kataklysm.content.mekaArm.bow.VanishParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.kataklysm.Deets.*;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, KATAKLYSM);

    public static final RegistryObject<ParticleType<SimpleParticleType>> VANISH;

    static {

        VANISH = REGISTRY.register("vanish", () -> new SimpleParticleType(true));

    }

    public static void register(ParticleFactoryRegisterEvent event){
        required(LogicalSide.CLIENT).run(() -> {
            Minecraft.getInstance().particleEngine.register(VANISH.get(), VanishParticle.Provider::new);
        });
    }
}
