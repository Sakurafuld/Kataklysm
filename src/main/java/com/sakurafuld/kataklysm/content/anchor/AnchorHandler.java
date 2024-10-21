package com.sakurafuld.kataklysm.content.anchor;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.sakurafuld.kataklysm.api.client.OutlineBuffer;
import com.sakurafuld.kataklysm.api.event.MixinEvent;
import com.sakurafuld.kataklysm.content.ModBlockEntities;
import com.sakurafuld.kataklysm.content.ModBlocks;
import mekanism.common.Mekanism;
import mekanism.common.network.to_client.PacketPortalFX;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;

import static com.sakurafuld.kataklysm.Deets.*;

@Mod.EventBusSubscriber(modid = KATAKLYSM)
public class AnchorHandler {
    public static final HashSet<BlockPos> ANCHORS = new HashSet<>();

    public static void addAnchor(BlockPos pos){
        ANCHORS.add(pos);
    }
    public static void removeAnchor(BlockPos pos){
        ANCHORS.remove(pos);
    }
    private static double getAngle(Vec3 positionVec, BlockPos anchor, float yRot, float xRot) {
        Vec3 blockVec = new Vec3((double) anchor.getX() + 0.5 - positionVec.x, (double) anchor.getY() + 1.0 - positionVec.y, (double) anchor.getZ() + 0.5 - positionVec.z).normalize();
        Vec3 lookVec = Vec3.directionFromRotation(xRot, yRot).normalize();
        return Math.acos(lookVec.dot(blockVec));
    }
    public static Optional<BlockPos> getTargetAnchorPos(Player p){
        Vec3 tweakedPos = p.getEyePosition().add(0, 0.5, 0);
        return ANCHORS.stream()
                .filter(target -> target.distToCenterSqr(tweakedPos) > 3 * 3)
                .filter(target -> Vec3.atCenterOf(target).closerThan(tweakedPos, Config.ANCHOR_DISTANCE.get()))
                .filter(target -> getAngle(tweakedPos, target, p.getYRot(), p.getXRot()) <  Math.toRadians(12 - Math.sqrt(Math.sqrt(Math.sqrt(target.distToCenterSqr(p.position()))))))
                .filter(anchor -> p.getLevel().isLoaded(anchor))
                .min(Comparator.comparingDouble(target -> Mth.abs((float) getAngle(tweakedPos, target, p.getYRot(), p.getXRot()))
                ));
    }
    public static void teleport(Player player, BlockPos pos, InteractionHand hand){


        Vec3 validPos = getValidPos(player, pos);


        if(validPos == null/*!isValid(player.getLevel(), pos.above(1)) || !isValid(player.level, pos.above(2))*/){
            player.sendMessage(new TranslatableComponent("chat.kataklysm.anchor.not_enough_space"), player.getUUID());
            return;
        }

        double distance = player.distanceToSqr(validPos.x(), validPos.y(), validPos.z());

        if (!((AnchorBlock) ModBlocks.ANCHOR.get()).drainEnergy(player, distance, player.getItemInHand(hand))) {
            player.sendMessage(new TranslatableComponent("chat.kataklysm.anchor.not_enough_energy"), player.getUUID());
            return;
        }

        if(player.isPassenger()) player.stopRiding();
        player.teleportToWithTicket(validPos.x(), validPos.y(), validPos.z());
        player.fallDistance = 0;
        Mekanism.packetHandler().sendToAllTracking(new PacketPortalFX(pos.above()), player.level, pos);
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    private static Vec3 getValidPos(Player player, BlockPos pos) {
        AABB playerAABB = walk(player.getBoundingBox(), new Vec3(pos.getX() + 0.5, pos.getY() + ((double) 13 / 16), pos.getZ() + 0.5));
//                                                                                               ２個分マイナスしてるけどよくわからんvec.length() * 2でも引けばいいのかな？.
        AABB penetrated = penetrate(player, playerAABB, new Vec3(0, (double) 1 / 16, 0), ((double) 1 / 16) + (player.getStepHeight()));

        if(penetrated == null) {
            return null;
        }

        return getLandingPos(penetrated);
    }
    private static AABB penetrate(Entity entity, AABB aabb, Vec3 vec, /*int max, */double distance) {
        if(vec.length() <= 1.0E-4) {
            LOG.debug("{}-TooShortLength", side());
            return aabb;
        }

        Vec3 origin = aabb.getCenter();

        while(true) {

            Iterable<VoxelShape> shapes = entity.getLevel().getBlockCollisions(null, aabb);

            boolean empty = true;

            for(VoxelShape shape : shapes) {
                if(!shape.isEmpty()) {
                    empty = false;
                }
            }

            if(empty)
                return aabb;

//            LOG.debug("{}-Distance-{}-{}", side(), Math.sqrt(origin.distanceToSqr(aabb.getCenter())), distance);
            if(Math.sqrt(origin.distanceToSqr(aabb.getCenter())) > distance )
                break;

            aabb = aabb.move(vec);
        }

        return null;
    }
    private static Vec3 getLandingPos(AABB aabb) {
        Vec3 center = aabb.getCenter();
        return new Vec3(center.x(), aabb.minY, center.z());
    }
    private static AABB walk(AABB origin, Vec3 target) {
        Vec3 o = origin.getCenter();
        o = new Vec3(o.x(), origin.minY, o.z());
        double x = target.x() - o.x();
        double y = target.y() - o.y();
        double z = target.z() - o.z();
        return new AABB(origin.minX + x, origin.minY + y, origin.minZ + z, origin.maxX + x, origin.maxY + y, origin.maxZ + z);
    }


    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickItem event){
        required(MEKANISM).run(()-> {
            if (event.getEntityLiving() instanceof Player p && p.isShiftKeyDown() && ((AnchorBlock)ModBlocks.ANCHOR.get()).canTeleport(event.getPlayer())){
                getTargetAnchorPos(event.getPlayer()).ifPresent(pos-> {
                    required(LogicalSide.SERVER).run(()->
                            teleport(event.getPlayer(), pos, event.getHand()));
                    p.swing(event.getHand());
                    event.setCanceled(true);
                });
            }
        });

    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderLevel(MixinEvent.RenderLevel event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = event.getPlayer();
        Level level = event.getLevel();
        PoseStack poseStack = event.getPoseStack();
        if(mc.options.hideGui)
            return;

        for(BlockPos pos : AnchorHandler.ANCHORS) {
            level.getBlockEntity(pos, ModBlockEntities.ANCHOR.get()).ifPresent(tile -> {
                Vec3 view = event.getCamera().getPosition();
                if(
                        canTeleport(player)
                        && level.isLoaded(pos)
                        && Vec3.atCenterOf(pos).closerThan(view.add(0, 0.5, 0), Config.ANCHOR_DISTANCE.get())
                        && tile.getBlockPos().distToCenterSqr(view.add(0, 0.5, 0)) > 3 * 3
                        && (AnchorHandler.getTargetAnchorPos(player).isEmpty() || !AnchorHandler.getTargetAnchorPos(player).get().equals(pos))

                ) {

                    double x = pos.getX() - view.x();
                    double y = pos.getY() - view.y();
                    double z = pos.getZ() - view.z();

                    double scale = Math.sqrt(tile.getBlockPos().distToCenterSqr(mc.player.getEyePosition().add(0, 0.5, 0)) * 0.01);
                    double multiplier = Math.pow(Math.toRadians(mc.options.fov), 2.0);
                    scale += multiplier;
                    Camera camera = mc.gameRenderer.getMainCamera();

                    poseStack.pushPose();

                    poseStack.translate(x, y, z);

                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.mulPose(camera.rotation());
                    poseStack.scale(1, 1, 0.0001f);
                    poseStack.translate(0, 0, 0.3);
                    poseStack.scale((float) -scale, (float) scale, (float) -scale);

                    mc.getItemRenderer()//                                                                             -------EnderIOをパクっただけなので何が起きているのか分からないゾーン-------
                            .render(tile.getIcon(), ItemTransforms.TransformType.GUI, false, poseStack, OutlineBuffer.INSTANCE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, mc.getItemRenderer().getModel(tile.getIcon(), tile.getLevel(), null, 0));

                    poseStack.popPose();


                    if(!tile.getName().isEmpty()){
                        poseStack.pushPose();
                        poseStack.translate(0.5, 0.7, 0.5);
                        poseStack.mulPose(camera.rotation());
                        poseStack.translate(0, 0, -0.3);
                        poseStack.scale((float) -scale * 0.025f, (float) -scale * 0.025f, (float) scale * 0.025f);

                        float textOpacitySetting = mc.options.getBackgroundOpacity(0.9f);
                        int alpha = (int) (textOpacitySetting * 255) << 24;

                        Matrix4f matrix = poseStack.last().pose();
                        OutlineBuffer buffer = OutlineBuffer.INSTANCE;
                        String name = tile.getName();

                        if(mc.font.isBidirectional())
                            name = mc.font.bidirectionalShaping(name);

                        float halfWidth = ((float) -mc.font.width(name) / 2);

                        mc.font.drawInBatch(name, halfWidth, 0, Color.WHITE.getRGB(), false, matrix, buffer, true, alpha, LightTexture.FULL_BRIGHT);
                        mc.font.drawInBatch(name, halfWidth, 0, Color.WHITE.getRGB(), false, matrix, buffer, false, 0, LightTexture.FULL_BRIGHT);
                        mc.renderBuffers().bufferSource().endBatch();
                        poseStack.popPose();
                    }
                    poseStack.popPose();
                }
            });
        }
    }
    public static boolean canTeleport(Player player) {
        return ModBlocks.ANCHOR != null && ((AnchorBlock) ModBlocks.ANCHOR.get()).canTeleport(player);
    }
}
