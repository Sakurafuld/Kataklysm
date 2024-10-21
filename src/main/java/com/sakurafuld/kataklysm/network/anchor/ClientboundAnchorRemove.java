package com.sakurafuld.kataklysm.network.anchor;

import com.sakurafuld.kataklysm.content.anchor.AnchorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundAnchorRemove {
    private final BlockPos anchor;
    public ClientboundAnchorRemove(BlockPos anchor){
        this.anchor = anchor;
    }
    public static void encode(ClientboundAnchorRemove msg, FriendlyByteBuf buffer){
        buffer.writeBlockPos(msg.anchor);
    }
    public static ClientboundAnchorRemove decode(FriendlyByteBuf buffer){
        return new ClientboundAnchorRemove(buffer.readBlockPos());
    }
    public void handle( Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()-> AnchorHandler.removeAnchor(this.anchor));
        ctx.get().setPacketHandled(true);
    }
}
