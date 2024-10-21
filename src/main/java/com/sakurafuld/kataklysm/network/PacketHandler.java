package com.sakurafuld.kataklysm.network;

import com.sakurafuld.kataklysm.Deets;
import com.sakurafuld.kataklysm.network.anchor.ClientboundAnchorRemove;
import com.sakurafuld.kataklysm.network.anchor.ClientboundAnchorUpdate;
import com.sakurafuld.kataklysm.network.anchor.ServerboundAnchorUpdate;
import com.sakurafuld.kataklysm.network.mekaArm.ClientboundArrowMove;
import com.sakurafuld.kataklysm.network.mekaArm.ClientboundArrowVanish;
import com.sakurafuld.kataklysm.network.solar.ClientboundSolarChunkUpdate;
import com.sakurafuld.kataklysm.network.touch.ClientboundRemoveTouchItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE
            = NetworkRegistry.newSimpleChannel(new ResourceLocation(Deets.KATAKLYSM, "main"), ()-> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void initialize() {
        int id = 0;

        INSTANCE.registerMessage(id++, ClientboundRemoveTouchItem.class, ClientboundRemoveTouchItem::encode, ClientboundRemoveTouchItem::decode, ClientboundRemoveTouchItem::handle);

        INSTANCE.registerMessage(id++, ClientboundArrowMove.class, ClientboundArrowMove::encode, ClientboundArrowMove::decode, ClientboundArrowMove::handle);
        INSTANCE.registerMessage(id++, ClientboundArrowVanish.class , ClientboundArrowVanish::encode, ClientboundArrowVanish::decode, ClientboundArrowVanish::handle);

        INSTANCE.registerMessage(id++, ClientboundSolarChunkUpdate.class, ClientboundSolarChunkUpdate::encode, ClientboundSolarChunkUpdate::decode, ClientboundSolarChunkUpdate::handle);


        INSTANCE.registerMessage(id++, ClientboundAnchorRemove.class, ClientboundAnchorRemove::encode, ClientboundAnchorRemove::decode, ClientboundAnchorRemove::handle);
        INSTANCE.registerMessage(id++, ClientboundAnchorUpdate.class, ClientboundAnchorUpdate::encode, ClientboundAnchorUpdate::decode, ClientboundAnchorUpdate::handle);
        INSTANCE.registerMessage(id++, ServerboundAnchorUpdate.class, ServerboundAnchorUpdate::encode, ServerboundAnchorUpdate::decode, ServerboundAnchorUpdate::handle);
    }
}
