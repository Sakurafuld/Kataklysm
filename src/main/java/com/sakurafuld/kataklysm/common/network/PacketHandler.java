package com.sakurafuld.kataklysm.common.network;

import com.sakurafuld.kataklysm.Kataklysm;
import com.sakurafuld.kataklysm.common.network.anchor.CS2SCAnchorUpdate;
import com.sakurafuld.kataklysm.common.network.anchor.C2SAnchorTeleport;
import com.sakurafuld.kataklysm.common.network.anchor.S2CAnchorRemove;
import com.sakurafuld.kataklysm.common.network.solar.S2CSolarChunkUpdate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE
            = NetworkRegistry.newSimpleChannel(new ResourceLocation(Kataklysm.ID, "main"), ()-> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    public static void init(){
        int id = 0;
        INSTANCE.registerMessage(id++, C2SAnchorTeleport.class, C2SAnchorTeleport::encode, C2SAnchorTeleport::decode, C2SAnchorTeleport::handle);
        INSTANCE.registerMessage(id++, S2CAnchorRemove.class, S2CAnchorRemove::encode, S2CAnchorRemove::decode, S2CAnchorRemove::handle);
        INSTANCE.registerMessage(id++, CS2SCAnchorUpdate.class, CS2SCAnchorUpdate::encode, CS2SCAnchorUpdate::decode, CS2SCAnchorUpdate::handle);
        INSTANCE.registerMessage(id++, S2CSolarChunkUpdate.class, S2CSolarChunkUpdate::encode, S2CSolarChunkUpdate::decode, S2CSolarChunkUpdate::handle);
    }
}
