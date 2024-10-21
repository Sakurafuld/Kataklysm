package com.sakurafuld.kataklysm.network.anchor;

import com.sakurafuld.kataklysm.content.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.kataklysm.Deets.*;

public class ClientboundAnchorUpdate {
    private ItemStack icon = ItemStack.EMPTY;
    private final BlockPos pos;

    public ClientboundAnchorUpdate(BlockPos pos, ItemStack icon){
        this.pos = pos;
        this.icon = icon;

    }
    public ClientboundAnchorUpdate(BlockPos pos){
        this.pos = pos;
    }
    public static void encode(ClientboundAnchorUpdate msg, FriendlyByteBuf buffer){
        buffer.writeBlockPos(msg.pos);
        buffer.writeItemStack(msg.icon, false);//send optimized NBT = false

    }
    public static ClientboundAnchorUpdate decode(FriendlyByteBuf buffer){
        return new ClientboundAnchorUpdate(buffer.readBlockPos(), buffer.readItem());
    }
    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {

            LOG.info("{}-AnchorUpdateHandle", side());
//            if(ctx.get().getSender() != null && ctx.get().getSender().level.hasChunkAt(this.pos)){
//                ctx.get().getSender().level.getBlockEntity(this.pos, ModBlockEntities.ANCHOR.get()).ifPresent(anchor->{
//
//                    if(!this.name.isEmpty()) anchor.setName(this.name);
//
//                    if(!this.icon.isEmpty()) anchor.setIcon(this.icon);
//
//                    anchor.setChanged();
//                } );
//            }

            if(Minecraft.getInstance().level != null && Minecraft.getInstance().level.hasChunkAt(this.pos)){
                Minecraft.getInstance().level.getBlockEntity(this.pos, ModBlockEntities.ANCHOR.get()).ifPresent(anchor->{

                    if(!this.icon.isEmpty()) anchor.setIcon(this.icon);

                    anchor.setChanged();
                } );
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
