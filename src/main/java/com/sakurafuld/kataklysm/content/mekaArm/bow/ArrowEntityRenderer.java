package com.sakurafuld.kataklysm.content.mekaArm.bow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.sakurafuld.kataklysm.Deets.*;

@OnlyIn(Dist.CLIENT)
public class ArrowEntityRenderer extends EntityRenderer<ArrowEntity> {
    public static final ResourceLocation TEXTURE = identifier(KATAKLYSM, "textures/entity/arrow.png");
    public static final ResourceLocation TEXTURE_SUB = identifier(KATAKLYSM, "textures/entity/arrow_sub.png");
    private final Model MODEL;
    public ArrowEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.MODEL = new ArrowEntityModel(ArrowEntityModel.create().bakeRoot());
    }

    @Override
    public ResourceLocation getTextureLocation(ArrowEntity pEntity) {
        return pEntity.isSub() ? TEXTURE_SUB : TEXTURE;
    }
    @Override
    public void render(ArrowEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();

        pPoseStack.scale(1.75f, 1.75f, 1.75f);
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(pPartialTick, pEntity.yRotO, pEntity.getYRot()) - 90f));
        pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(pPartialTick, pEntity.xRotO, pEntity.getXRot()) + 90f));
        pPoseStack.translate(0, -0.8, 0);
        this.MODEL.renderToBuffer(pPoseStack, pBuffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(pEntity))), pPackedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        pPoseStack.popPose();
    }
}
