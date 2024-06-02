package com.sakurafuld.kataklysm.common.network.anchor;

import com.sakurafuld.kataklysm.client.render.block.AnchorBlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CAnchorRemove {
    private final BlockPos anchor;
    public S2CAnchorRemove(BlockPos anchor){
        this.anchor = anchor;
    }
    public static void encode(S2CAnchorRemove msg, FriendlyByteBuf buffer){
        buffer.writeBlockPos(msg.anchor);
    }
    public static S2CAnchorRemove decode(FriendlyByteBuf buffer){
        return new S2CAnchorRemove(buffer.readBlockPos());
    }
    public static void handle(S2CAnchorRemove msg, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->  AnchorBlockEntityRenderer.getAnchors().remove(msg.anchor));
        ctx.get().setPacketHandled(true);
    }
}
