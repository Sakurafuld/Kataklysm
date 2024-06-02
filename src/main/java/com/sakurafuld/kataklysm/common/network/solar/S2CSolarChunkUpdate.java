package com.sakurafuld.kataklysm.common.network.solar;

import com.sakurafuld.kataklysm.common.capability.SolarChunkProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSolarChunkUpdate {
    private final BlockPos POS;
    private final boolean ACTIVE;

    public S2CSolarChunkUpdate(BlockPos pos, boolean active){
        this.POS = pos;
        this.ACTIVE = active;
    }

    public static void encode(S2CSolarChunkUpdate msg, FriendlyByteBuf buf){
        buf.writeBlockPos(msg.POS);
        buf.writeBoolean(msg.ACTIVE);
    }
    public static S2CSolarChunkUpdate decode(FriendlyByteBuf buf){
        return new S2CSolarChunkUpdate(buf.readBlockPos(), buf.readBoolean());
    }
    public static void handle(S2CSolarChunkUpdate msg, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().level.getChunkAt(msg.POS).getCapability(SolarChunkProvider.SOLAR)
                        .ifPresent(solar -> {
                            solar.setActive(msg.ACTIVE ? msg.POS : null);
                        });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
