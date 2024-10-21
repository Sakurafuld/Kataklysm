package com.sakurafuld.kataklysm.mixin.mekaArm;

import com.sakurafuld.kataklysm.content.mekaArm.bow.ArrowEntity;
import com.sakurafuld.kataklysm.network.PacketHandler;
import com.sakurafuld.kataklysm.network.mekaArm.ClientboundArrowMove;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.Consumer;

import static com.sakurafuld.kataklysm.Deets.*;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {

    @Shadow @Final private Entity entity;

    @Inject(method = {"sendChanges()V"}, at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 2), cancellable = true)
    private void onVelocityBow(CallbackInfo ci) {
        if(this.entity instanceof ArrowEntity arrow) {
            LOG.info("{}-sendChangesMk2", side());
            PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundArrowMove(arrow, false));
            ci.cancel();
        }
    }
    @Inject(method = {"sendPairingData(Ljava/util/function/Consumer;)V"}, at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 3), cancellable = true)
    private void onPairingDataBow(Consumer<Packet<?>> pPacketConsumer, CallbackInfo ci) {
        if(this.entity instanceof ArrowEntity arrow) {
            LOG.info("{}-sendPairingData", side());
            PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundArrowMove(arrow, false));
            ci.cancel();
        }
    }
    @Inject(method = {"sendChanges()V"}, at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 3), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onMovePacketBow(CallbackInfo ci, List<Object> list, int l, int k1, Vec3 vec3, boolean flag2, Packet<ClientGamePacketListener> packet1) {
        if(this.entity instanceof ArrowEntity arrow) {
            LOG.info("{}-sendChangesMk3={}", side(), packet1.getClass().getSimpleName());
            if(packet1 instanceof ClientboundMoveEntityPacket.PosRot) {
                PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundArrowMove(arrow, true));
                ci.cancel();
            }
        }
    }
}
