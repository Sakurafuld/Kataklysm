package com.sakurafuld.kataklysm.network.mekaArm;

import com.sakurafuld.kataklysm.content.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class ClientboundArrowVanish {
    private final Vec3 POSITION;
    private final int COUNT;

    public ClientboundArrowVanish(Vec3 position, int count){
        this.POSITION = position;
        this.COUNT = count;

    }

    public static void encode(ClientboundArrowVanish msg, FriendlyByteBuf buf){
        buf.writeDouble(msg.POSITION.x());
        buf.writeDouble(msg.POSITION.y());
        buf.writeDouble(msg.POSITION.z());
        buf.writeInt(msg.COUNT);
    }
    public static ClientboundArrowVanish decode(FriendlyByteBuf buf){
        return new ClientboundArrowVanish(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readInt());
    }
    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            if(Minecraft.getInstance().level != null) {
                Random random = new Random(this.POSITION.hashCode());
                for(int count = 0; count < this.COUNT; count++) {
                    Minecraft.getInstance().level.addParticle((ParticleOptions) ModParticles.VANISH.get(), this.POSITION.x() + ((random.nextFloat() * 2 - 1) * 0.25), this.POSITION.y()/* + (random.nextFloat() - 0.5f)*/, this.POSITION.z() + ((random.nextFloat() * 2 - 1) * 0.25), 0, 0, 0);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
