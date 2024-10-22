package com.sakurafuld.kataklysm.content.anchor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class AnchorOverlay implements IIngameOverlay {
    private static final Color BACK = new Color(0f, 0f, 0f, 0.7f);
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

            int centerX = width / 2;
            int centerY = height / 2;

            gui.setupOverlayRenderState(true, false);

            poseStack.pushPose();

            mc.getItemRenderer().renderGuiItem(anchor.getIcon(), centerX - 8, centerY - 8 + 12 + 8);

            poseStack.popPose();

            String name = anchor.getName();
            if(!name.isEmpty()){
                if(mc.font.isBidirectional())
                    name = mc.font.bidirectionalShaping(name);
                float halfWidth = (float) -mc.font.width(name) / 2;
                boolean error = AnchorHandler.getValidPos(player, target) == null;
                int mainColor = error ? Color.RED.getRGB() : AnchorHandler.COLOR.getRGB();

                int left = Mth.floor(halfWidth - 2) - 1;
                int up = 0 - 1;
                int right = Mth.floor(-halfWidth + 2) + 1;
                int down = mc.font.lineHeight + 1;

                poseStack.pushPose();
                poseStack.translate(centerX, centerY - 14 - 8, 0);
                poseStack.scale(1.3f, 1.3f, 1);

//                mc.font.drawInBatch(name, halfWidth, 0, color, false, poseStack.last().pose(), mc.renderBuffers().bufferSource(), true, alpha, LightTexture.FULL_BRIGHT);
                mc.font.drawInBatch(name, halfWidth, 0, mainColor, false, poseStack.last().pose(), mc.renderBuffers().bufferSource(), false, 0, LightTexture.FULL_BRIGHT);
                GuiComponent.fill(poseStack, left, up, right, down, BACK.getRGB());

                GuiComponent.fill(poseStack, left, up, right, up + 1, mainColor);
                GuiComponent.fill(poseStack, right - 1, up, right, down, mainColor);
                GuiComponent.fill(poseStack, left, down, right, down - 1, mainColor);
                GuiComponent.fill(poseStack, left, up, left + 1, down, mainColor);

                poseStack.popPose();
                mc.renderBuffers().bufferSource().endBatch();
            }
        });
    }
}
