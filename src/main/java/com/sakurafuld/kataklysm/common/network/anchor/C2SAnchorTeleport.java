package com.sakurafuld.kataklysm.common.network.anchor;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SAnchorTeleport {
    private final BlockPos pos;
    private final InteractionHand hand;
    public C2SAnchorTeleport(BlockPos pos, InteractionHand hand){
        this.pos = pos;
        this.hand = hand;
    }

    public static void encode(C2SAnchorTeleport msg, FriendlyByteBuf buffer){
        buffer.writeBlockPos(msg.pos);
        buffer.writeEnum(msg.hand);
    }
    public static C2SAnchorTeleport decode(FriendlyByteBuf buffer){
        return new C2SAnchorTeleport(buffer.readBlockPos(), buffer.readEnum(InteractionHand.class));
    }

    public static void handle(C2SAnchorTeleport msg, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            ServerPlayer player = ctx.get().getSender();

            if(ModList.get().isLoaded("mekanism")){
                double distance = player.distanceToSqr(msg.pos.getX(), msg.pos.getY(), msg.pos.getZ());
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(player.getItemInHand(msg.hand), 0);
                FloatingLong energyNeeded = MekanismConfig.gear.mekaToolEnergyUsageTeleport.get().multiply(distance / 20);
                if (energyContainer == null || energyContainer.getEnergy().smallerThan(energyNeeded)){
                    player.sendMessage(new TranslatableComponent("chat.kataklysm.anchor.not_enough_energy"), player.getUUID());
                    return;
                }
                energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);
            }

            if(!isValidDestinationBlock(player.level, msg.pos.above(1)) || !isValidDestinationBlock(player.level, msg.pos.above(2))){
                player.sendMessage(new TranslatableComponent("chat.kataklysm.anchor.not_enough_space"), player.getUUID());
                return;
            }


            if(player.isPassenger()) player.stopRiding();
            player.teleportToWithTicket(msg.pos.getX() + 0.5, msg.pos.getY() + 0.9, msg.pos.getZ() + 0.5);
            player.fallDistance = 0;
            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        });
        ctx.get().setPacketHandled(true);
    }
    private static boolean isValidDestinationBlock(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isAir() || blockState.getBlock() instanceof LiquidBlock || blockState.getBlock() instanceof IFluidBlock;
    }
}
