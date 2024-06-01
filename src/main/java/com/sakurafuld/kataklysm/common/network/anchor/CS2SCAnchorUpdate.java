package com.sakurafuld.kataklysm.common.network.anchor;

import com.sakurafuld.kataklysm.common.init.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CS2SCAnchorUpdate {
    private String name = "";
    private ItemStack icon = ItemStack.EMPTY;
    private final BlockPos pos;

    public CS2SCAnchorUpdate(BlockPos pos, String name, ItemStack icon){
        this.pos = pos;
        this.name = name;
        this.icon = icon;

    }
    public CS2SCAnchorUpdate(BlockPos pos, String name){
        this.pos = pos;
        this.name = name;

    }
    public CS2SCAnchorUpdate(BlockPos pos, ItemStack icon){
        this.pos = pos;
        this.icon = icon;

    }
    public CS2SCAnchorUpdate(BlockPos pos){
        this.pos = pos;
    }
    public static void encode(CS2SCAnchorUpdate msg, FriendlyByteBuf buffer){
        buffer.writeBlockPos(msg.pos);
        buffer.writeUtf(msg.name);
        buffer.writeItemStack(msg.icon, false);//send optimized NBT = false

    }
    public static CS2SCAnchorUpdate decode(FriendlyByteBuf buffer){
        return new CS2SCAnchorUpdate(buffer.readBlockPos(), buffer.readUtf(), buffer.readItem());
    }
    public static void handle(CS2SCAnchorUpdate msg, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if(ctx.get().getSender() != null && ctx.get().getSender().level.hasChunkAt(msg.pos)){
                ctx.get().getSender().level.getBlockEntity(msg.pos, ModBlockEntities.ANCHOR.get()).ifPresent(anchor->{

                    if(!msg.name.isEmpty()) anchor.setName(msg.name);

                    if(!msg.icon.isEmpty()) anchor.setIcon(msg.icon);

                    anchor.setChanged();
                } );
            }

            if(Minecraft.getInstance().level != null && Minecraft.getInstance().level.hasChunkAt(msg.pos)){
                Minecraft.getInstance().level.getBlockEntity(msg.pos, ModBlockEntities.ANCHOR.get()).ifPresent(anchor->{

                    if(!msg.name.isEmpty()) anchor.setName(msg.name);

                    if(!msg.icon.isEmpty()) anchor.setIcon(msg.icon);

                    anchor.setChanged();
                } );
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
