package com.sakurafuld.kataklysm.network.touch;

import com.mojang.datafixers.util.Pair;
import com.sakurafuld.kataklysm.api.touch.TouchableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSyncTouchedBlocks {
    private final boolean INCREASE;
    private final BlockPos BLOCK;
    private final BlockPos POS;
    private final Direction FACE;


    public ClientboundSyncTouchedBlocks(boolean increase, BlockPos block, BlockPos pos, Direction face) {
        this.INCREASE = increase;
        this.BLOCK = block;
        this.POS = pos;
        this.FACE = face;
    }
    
    public static void encode(ClientboundSyncTouchedBlocks msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.INCREASE);
        buf.writeBlockPos(msg.BLOCK);
        buf.writeBlockPos(msg.POS);
        buf.writeEnum(msg.FACE);
    }
    public static ClientboundSyncTouchedBlocks decode(FriendlyByteBuf buf) {
        return new ClientboundSyncTouchedBlocks(buf.readBoolean(), buf.readBlockPos(), buf.readBlockPos(), buf.readEnum(Direction.class));
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(Minecraft.getInstance().level.getBlockEntity(this.BLOCK) instanceof TouchableBlockEntity touchable) {
                Pair<BlockPos, Direction> key = new Pair<>(this.POS, this.FACE);
                if(this.INCREASE) {
                    touchable.getTouchedBlocks().computeIfPresent(key, (pair, count) -> ++count);
                    touchable.getTouchedBlocks().putIfAbsent(key, 1);
                } else {
                    touchable.getTouchedBlocks().computeIfPresent(key, (pair, count) -> --count > 0 ? count : null);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
