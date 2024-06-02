package com.sakurafuld.kataklysm.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.sakurafuld.kataklysm.client.render.OutlineBuffer;
import com.sakurafuld.kataklysm.common.block.anchor.AnchorBlock;
import com.sakurafuld.kataklysm.common.block.anchor.AnchorBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;

public class AnchorBlockEntityRenderer implements BlockEntityRenderer<AnchorBlockEntity> {
    private static final HashSet<BlockPos> anchors = new HashSet<>();
    public AnchorBlockEntityRenderer(BlockEntityRendererProvider.Context context){}

    @Override
    public boolean shouldRenderOffScreen(AnchorBlockEntity tile) {
        return Vec3.atCenterOf(tile.getBlockPos()).closerThan(Minecraft.getInstance().getBlockEntityRenderDispatcher().camera.getPosition().add(0, 0.5, 0), 320);
    }

    public static HashSet<BlockPos> getAnchors(){
        return anchors;
    }
    @Override
    public boolean shouldRender(AnchorBlockEntity tile, Vec3 cameraPos) {

        double distance = Vec3.atCenterOf(tile.getBlockPos()).distanceToSqr(cameraPos.add(0, 0.5,0));
        return distance > 3 * 3 && 320 * 320 > distance && (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCrouching()) && isItemVisible();
    }

    @Override
    public void render(AnchorBlockEntity tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if(getTargetAnchorPos().isPresent() && getTargetAnchorPos().get().equals(tile.getBlockPos()))
            return;

        Minecraft mc = Minecraft.getInstance();
        double scale = Math.sqrt(tile.getBlockPos().distToCenterSqr(mc.getBlockEntityRenderDispatcher().camera.getPosition().add(0, 0.5, 0)) * 0.01);
        double multiplier = Math.pow(Math.toRadians(mc.options.fov), 2.0);
        scale += multiplier;
        Camera camera = mc.gameRenderer.getMainCamera();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        poseStack.scale((float) -scale, (float) scale, (float) -scale);
        mc.getItemRenderer()//                                                                             -------EnderIOをパクっただけなので何が起きているのか分からないゾーン-------
                .render(tile.getIcon(), ItemTransforms.TransformType.GUI, false, poseStack, OutlineBuffer.INSTANCE, 15728881, OverlayTexture.NO_OVERLAY, mc.getItemRenderer().getModel(tile.getIcon(), tile.getLevel(), null, 0));
        poseStack.popPose();
        poseStack.pushPose();
        if(!tile.getName().isEmpty()){
            poseStack.translate(0.5, 0.7, 0.5);
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale((float) -scale * 0.025f, (float) -scale * 0.025f, (float) scale * 0.025f);
            float textOpacitySetting = mc.options.getBackgroundOpacity(0.25f);
            int alpha = (int) (textOpacitySetting * 255) << 24;
            int nameColor = FastColor.ARGB32.color(255, 255, 255, 255);
            Matrix4f matrix = poseStack.last().pose();
            OutlineBuffer buffer = OutlineBuffer.INSTANCE;
            String name = tile.getName();
            if(mc.font.isBidirectional()) name = mc.font.bidirectionalShaping(name);
            float halfWidth = ((float) -mc.font.width(name) / 2);
            mc.font.drawInBatch(name, halfWidth, 0, nameColor, false, matrix, buffer, true, alpha, LightTexture.pack(15, 15));
            mc.font.drawInBatch(name, halfWidth, 0, nameColor, false, matrix, buffer, false, 0, LightTexture.pack(15, 15));
            mc.renderBuffers().bufferSource().endBatch();
        }
        poseStack.popPose();

    }

    private static double getAngleRadians(Vec3 positionVec, BlockPos anchor, float yRot, float xRot) {
        Vec3 blockVec = new Vec3((double) anchor.getX() + 0.5 - positionVec.x, (double) anchor.getY() + 1.0 - positionVec.y, (double) anchor.getZ() + 0.5 - positionVec.z).normalize();
        Vec3 lookVec = Vec3.directionFromRotation(xRot, yRot).normalize();
        return Math.acos(lookVec.dot(blockVec));
    }
    @OnlyIn(Dist.CLIENT)
    public static Optional<BlockPos> getTargetAnchorPos(){
        Player p = Minecraft.getInstance().player;
        Vec3 tweakedPos = Minecraft.getInstance().getBlockEntityRenderDispatcher().camera.getPosition().add(0, 0.5, 0);
        return anchors.stream()
                .filter(target -> Vec3.atCenterOf(target).distanceToSqr(tweakedPos) > 3 * 3)
                .filter(target -> Vec3.atCenterOf(target).closerThan(tweakedPos, 320))
                .filter(target -> getAngleRadians(tweakedPos, target, p.getYRot(), p.getXRot()) <  Math.toRadians(7 - Math.sqrt(Math.sqrt(Math.sqrt(target.distToCenterSqr(p.position()))))))
                .filter(anchor -> Minecraft.getInstance().level != null && Minecraft.getInstance().level.hasChunkAt(anchor))
                .min(Comparator.comparingDouble(target -> Mth.abs((float) getAngleRadians(tweakedPos, target, p.getYRot(), p.getXRot()))
    ));
    }
    @OnlyIn(Dist.CLIENT)
    public static boolean isItemVisible(){
        LocalPlayer p = Minecraft.getInstance().player;
        if(p == null) return false;
        return AnchorBlock.isAnchoringItem(p.getItemInHand(InteractionHand.MAIN_HAND))  || AnchorBlock.isAnchoringItem(p.getItemInHand(InteractionHand.OFF_HAND));
    }
}
