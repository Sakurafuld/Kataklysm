package com.sakurafuld.kataklysm.content.anchor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class AnchorOverlay implements IIngameOverlay {

    @Override
    public void render(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if(mc.options.hideGui || player == null || level == null || !AnchorHandler.canTeleport(player))
            return;
        AnchorHandler.getTargetAnchorPos(player).ifPresent(target -> {
            if(!(level.getBlockEntity(target) instanceof AnchorBlockEntity anchor))
                return;
            gui.setupOverlayRenderState(true, false);
            poseStack.pushPose();

            int centerX = width / 2;
            int centerY = height / 2;

            mc.getItemRenderer().renderGuiItem(anchor.getIcon(), centerX - 8, centerY - 8 + 12);

            String name = anchor.getName();
            if(!name.isEmpty()){
                if(mc.font.isBidirectional()) name = mc.font.bidirectionalShaping(name);
                float halfWidth = (float) -mc.font.width(name) / 2;
                int alpha = (int) (mc.options.getBackgroundOpacity(0.5f) * 255) << 24;

                poseStack.pushPose();
                poseStack.translate(centerX, centerY - 14, 0);

                mc.font.drawInBatch(name, halfWidth, 0, Color.WHITE.getRGB(), false, poseStack.last().pose(), mc.renderBuffers().bufferSource(), true, alpha, LightTexture.FULL_BRIGHT);
                mc.font.drawInBatch(name, halfWidth, 0, Color.WHITE.getRGB(), false, poseStack.last().pose(), mc.renderBuffers().bufferSource(), false, 0, LightTexture.FULL_BRIGHT);

                poseStack.popPose();
                mc.renderBuffers().bufferSource().endBatch();
            }

            poseStack.popPose();
        });
    }
}
