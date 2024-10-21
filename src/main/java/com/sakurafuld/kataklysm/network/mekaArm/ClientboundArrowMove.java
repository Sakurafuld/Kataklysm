package com.sakurafuld.kataklysm.network.mekaArm;

import com.sakurafuld.kataklysm.content.mekaArm.bow.ArrowEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.kataklysm.Deets.*;

public class ClientboundArrowMove {
    private final int ID;
    private final Vec3 DELTA;
    private final boolean MOVEMENT;
    private final float XROT;
    private final float YROT;
    private final boolean ONGROUND;

    public ClientboundArrowMove(ArrowEntity arrow, boolean movement){
        this.ID = arrow.getId();
        this.DELTA = arrow.getDeltaMovement();
        this.MOVEMENT = movement;
        if(movement){
            this.XROT = arrow.getXRot();
            this.YROT = arrow.getYRot();
            this.ONGROUND = arrow.isOnGround();
        } else {
            this.XROT = this.YROT = Float.MAX_VALUE;
            this.ONGROUND = false;
        }
    }
    private ClientboundArrowMove(FriendlyByteBuf buf){
        this.ID = buf.readInt();
        this.DELTA = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.MOVEMENT = buf.readBoolean();
        if(this.MOVEMENT){
            this.XROT = buf.readFloat();
            this.YROT = buf.readFloat();
            this.ONGROUND = buf.readBoolean();
        } else {
            this.XROT = this.YROT = Float.MAX_VALUE;
            this.ONGROUND = false;
        }
    }

    public static void encode(ClientboundArrowMove msg, FriendlyByteBuf buf){
        buf.writeInt(msg.ID);
        buf.writeDouble(msg.DELTA.x());
        buf.writeDouble(msg.DELTA.y());
        buf.writeDouble(msg.DELTA.z());
        buf.writeBoolean(msg.MOVEMENT);
        if(msg.MOVEMENT){
            buf.writeFloat(msg.XROT);
            buf.writeFloat(msg.YROT);
            buf.writeBoolean(msg.ONGROUND);
        }
    }
    public static ClientboundArrowMove decode(FriendlyByteBuf buf){
        return new ClientboundArrowMove(buf);
    }
    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            if(ctx.get().getDirection().getReceptionSide().isServer()){
                LOG.info("{}-ArrowMoveIsServer", side());
                return;
            }
                Minecraft mc = Minecraft.getInstance();
                if(mc.level != null){
                    Entity arrow = mc.level.getEntity(this.ID);
                    if(arrow != null){
                        arrow.lerpMotion(this.DELTA.x(), this.DELTA.y(), this.DELTA.z());
                        if(this.MOVEMENT){
                            arrow.setXRot(this.XROT);
                            arrow.setYRot(this.YROT);
                            arrow.setOnGround(this.ONGROUND);
                        }
                    }
                }
            }
        );
        ctx.get().setPacketHandled(true);
    }
}
