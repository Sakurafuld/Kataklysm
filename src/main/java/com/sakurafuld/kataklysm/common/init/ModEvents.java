package com.sakurafuld.kataklysm.common.init;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sakurafuld.kataklysm.Kataklysm;
import com.sakurafuld.kataklysm.client.render.block.AnchorBlockEntityRenderer;
import com.sakurafuld.kataklysm.common.block.anchor.AnchorBlock;
import com.sakurafuld.kataklysm.common.capability.SolarChunkProvider;
import com.sakurafuld.kataklysm.common.network.PacketHandler;
import com.sakurafuld.kataklysm.common.network.anchor.C2SAnchorTeleport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;


@Mod.EventBusSubscriber(modid = Kataklysm.ID)
public class ModEvents {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickItem event){
        if (event.getEntityLiving() instanceof Player p && p.isCrouching() && AnchorBlock.isAnchoringItem(event.getItemStack())){
            if(p.level.isClientSide) AnchorBlockEntityRenderer.getTargetAnchorPos().ifPresent(pos-> {
                PacketHandler.INSTANCE.sendToServer(new C2SAnchorTeleport(pos, event.getHand()));
            });
            p.swing(event.getHand());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHUD(RenderGameOverlayEvent.PostLayer event){
        AnchorBlockEntityRenderer.getTargetAnchorPos().ifPresent(anchor->{
            Minecraft mc = Minecraft.getInstance();
            if(!(mc.level != null && mc.player != null && mc.player.isCrouching() && AnchorBlockEntityRenderer.isItemVisible()))
                return;


            mc.level.getBlockEntity(anchor, ModBlockEntities.ANCHOR.get()).ifPresent(anchorBlockEntity -> {
                ProfilerFiller profiler = mc.getProfiler();

                int centerWidth = event.getWindow().getGuiScaledWidth() / 2;
                int centerHeight = event.getWindow().getGuiScaledHeight() / 2;

                profiler.push("anchor");
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                mc.getItemRenderer().renderGuiItem(anchorBlockEntity.getIcon(), centerWidth - 8, centerHeight - 8 + 12);
                String name = anchorBlockEntity.getName();
                if(!name.isEmpty()){
                    event.getMatrixStack().pushPose();
                    event.getMatrixStack().translate(centerWidth, centerHeight - 14, 0);
                    if(mc.font.isBidirectional()) name = mc.font.bidirectionalShaping(name);
                    float halfWidth = (float) -mc.font.width(name) / 2;
                    GuiComponent.fill(event.getMatrixStack(), (int) Math.floor(halfWidth - 5), -1, (int) Math.floor(-halfWidth + 5), 8, FastColor.ARGB32.color(170, 0, 0, 0));
                    mc.font.drawInBatch(name, halfWidth, 0, FastColor.ARGB32.color(255, 255, 255, 255), false, event.getMatrixStack().last().pose(), mc.renderBuffers().bufferSource(), false, 0, 15728880, false);
                    event.getMatrixStack().popPose();
                    mc.renderBuffers().bufferSource().endBatch();
                }
                RenderSystem.disableBlend();
                profiler.pop();
            });

        });
    }
    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<LevelChunk> event){
        if(!event.getObject().getCapability(SolarChunkProvider.SOLAR).isPresent()){
            event.addCapability(new ResourceLocation(Kataklysm.ID, "solar"), new SolarChunkProvider());
        }
    }
}
