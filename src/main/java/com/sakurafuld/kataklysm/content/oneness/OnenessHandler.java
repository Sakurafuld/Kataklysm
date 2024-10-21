package com.sakurafuld.kataklysm.content.oneness;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.sakurafuld.kataklysm.api.capability.TouchItemStack;
import com.sakurafuld.kataklysm.api.event.MixinEvent;
import com.sakurafuld.kataklysm.api.touch.TouchHandler;
import com.sakurafuld.kataklysm.api.touch.TouchableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sakurafuld.kataklysm.Deets.*;

@Mod.EventBusSubscriber(modid = KATAKLYSM, value = Dist.CLIENT)
public class OnenessHandler {
    private static final Color BLOCK = new Color(0.8f, 0, 0, 0.1f);
    private static final Color ORIGIN = new Color(0.5f, 0.5f, 0.5f, 0.1f);
    private static final ResourceLocation LOCATION = identifier(KATAKLYSM, "textures/gui/oneness.png");

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderLevel(MixinEvent.RenderLevel event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = event.getPlayer();
        Level level = event.getLevel();
        PoseStack poseStack = event.getPoseStack();

        TouchHandler.get(player).ifPresent(stack -> stack.getCapability(TouchItemStack.CAPABILITY).ifPresent(touch -> {
            if(stack.getItem() instanceof OnenessTouchItem && level.getBlockEntity(touch.getFromPos()) instanceof TouchableBlockEntity touchable) {

                VertexConsumer vertexConsumer = mc.renderBuffers().bufferSource().getBuffer(OnenessBlockEntityRenderer.Render.TYPE);

                Vec3 view = event.getCamera().getPosition();
                BlockPos origin = touchable.getBlockPos().relative(touchable.getBlockState().getValue(BlockStateProperties.FACING_HOPPER));
                double x = origin.getX() - view.x();
                double y = origin.getY() - view.y();
                double z = origin.getZ() - view.z();

                Origin : {
                    poseStack.pushPose();
                    poseStack.translate(x, y, z);

                    poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                    render(poseStack.last().pose(), vertexConsumer, ORIGIN, 1);

                    poseStack.popPose();
                }

                Set<Pair<BlockPos, Direction>> keySet = touchable.getTouchedBlocks().keySet();
                Set<BlockPos> posSet = keySet.parallelStream().map(Pair::getFirst).collect(Collectors.toSet());


                for(Pair<BlockPos, Direction> pair : touchable.getTouchedBlocks().keySet()) {
                    BlockPos pos = pair.getFirst();
                    x = pos.getX() - view.x();
                    y = pos.getY() - view.y();
                    z = pos.getZ() - view.z();

                    poseStack.pushPose();
                    poseStack.translate(x, y, z);


                    Cube : {
                        poseStack.pushPose();

                        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                        render(poseStack.last().pose(), vertexConsumer, BLOCK, 1, pos, posSet);

                        poseStack.popPose();
                    }

                    Face : {
                        poseStack.pushPose();

                        Vec3i normal = pair.getSecond().getNormal();
                        poseStack.translate((double) normal.getX() / 3, (double) normal.getY() / 3, (double) normal.getZ() / 3);
                        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                        render(poseStack.last().pose(), vertexConsumer, Color.YELLOW, 0.6f);

                        poseStack.popPose();
                    }

                    poseStack.popPose();
                }
                mc.renderBuffers().bufferSource().endBatch(OnenessBlockEntityRenderer.Render.TYPE);
            }
        }));
    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void hud(RenderGameOverlayEvent.PreLayer event) {
        Minecraft mc = Minecraft.getInstance();

        if(mc.player == null || TouchHandler.get(mc.player).isEmpty())
            return;

        if(event.getOverlay() == ForgeIngameGui.EXPERIENCE_BAR_ELEMENT) {
            PoseStack stack = event.getMatrixStack();

            Window window = event.getWindow();
            //真ん中X.
            int centerX = window.getScreenWidth() / 4;
            //一番下Y.
            int centerY = window.getScreenHeight() / 2;

            int leftX = centerX - (18 * 5) - 1;
            int upY = centerY - 22 - 5 - 2;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, LOCATION);
            GuiComponent.blit(stack, leftX, upY, 0, 0, 182, 5, 182, 5);

            event.setCanceled(true);
        }
    }
    private static void render(Matrix4f matrix, VertexConsumer builder, Color color, float scale) {
        render(matrix, builder, color, scale, null, null);
    }
    private static void render(Matrix4f matrix, VertexConsumer builder, Color color, float scale, BlockPos pos, Set<BlockPos> posSet) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = 0.5f;

        float startX = 0 + (1 - scale) / 2;
        float startY = 0 + (1 - scale) / 2;
        float startZ = -1 + (1 - scale) / 2;
        float endX = 1 - (1 - scale) / 2;
        float endY = 1 - (1 - scale) / 2;
        float endZ = 0 - (1 - scale) / 2;

        boolean flag = pos == null || posSet == null || scale != 1;

        if(flag || !posSet.contains(pos.relative(Direction.DOWN))) {
            float normalX = Direction.DOWN.getStepX(), normalY = Direction.DOWN.getStepY(), normalZ = Direction.DOWN.getStepZ();
            builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).normal(normalX, normalY, normalZ).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.UP))) {
            float normalX = Direction.UP.getStepX(), normalY = Direction.UP.getStepY(), normalZ = Direction.UP.getStepZ();
            builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.EAST))) {
            float normalX = Direction.EAST.getStepX(), normalY = Direction.EAST.getStepY(), normalZ = Direction.EAST.getStepZ();
            builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.WEST))) {
            float normalX = Direction.WEST.getStepX(), normalY = Direction.WEST.getStepY(), normalZ = Direction.WEST.getStepZ();
            builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.SOUTH))) {
            float normalX = Direction.SOUTH.getStepX(), normalY = Direction.SOUTH.getStepY(), normalZ = Direction.SOUTH.getStepZ();
            builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.NORTH))) {
            float normalX = Direction.NORTH.getStepX(), normalY = Direction.NORTH.getStepY(), normalZ = Direction.NORTH.getStepZ();
            builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
            builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
        }
    }
}
