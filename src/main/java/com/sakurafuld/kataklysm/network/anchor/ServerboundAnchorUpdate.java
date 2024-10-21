package com.sakurafuld.kataklysm.network.anchor;

import com.sakurafuld.kataklysm.content.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.kataklysm.Deets.LOG;
import static com.sakurafuld.kataklysm.Deets.side;

public class ServerboundAnchorUpdate {
    private final String name;
    private final BlockPos pos;

    public ServerboundAnchorUpdate(BlockPos pos, String name){
        this.pos = pos;
        this.name = name;
    }

    public static void encode(ServerboundAnchorUpdate msg, FriendlyByteBuf buffer){
        buffer.writeBlockPos(msg.pos);
        buffer.writeUtf(msg.name);
    }
    public static ServerboundAnchorUpdate decode(FriendlyByteBuf buffer){
        return new ServerboundAnchorUpdate(buffer.readBlockPos(), buffer.readUtf());
    }
    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {

            LOG.info("{}-AnchorUpdateHandle", side());
            if(ctx.get().getSender() != null && ctx.get().getSender().level.hasChunkAt(this.pos)){
                ctx.get().getSender().level.getBlockEntity(this.pos, ModBlockEntities.ANCHOR.get()).ifPresent(anchor->{

                    if(!this.name.isEmpty()) anchor.setName(this.name);

                    anchor.setChanged();
                } );
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
