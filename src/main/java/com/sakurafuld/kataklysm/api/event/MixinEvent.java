package com.sakurafuld.kataklysm.api.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;

public class MixinEvent extends Event {
    @OnlyIn(Dist.CLIENT)
    public static class RenderLevel extends MixinEvent {
        private final LocalPlayer player;
        private final ClientLevel level;
        private final PoseStack poseStack;
        private final Camera camera;
        private final GameRenderer gameRenderer;
        private final float partialTicks;

        public RenderLevel(ClientLevel level, LocalPlayer player, PoseStack poseStack, Camera camera, GameRenderer gameRenderer, float partialTicks) {
            this.player = player;
            this.level = level;
            this.poseStack = poseStack;
            this.camera = camera;
            this.gameRenderer = gameRenderer;
            this.partialTicks = partialTicks;
        }

        public LocalPlayer getPlayer() {
            return this.player;
        }
        public ClientLevel getLevel() {
            return this.level;
        }
        public PoseStack getPoseStack() {
            return this.poseStack;
        }
        public Camera getCamera() {
            return this.camera;
        }
        public GameRenderer getGameRenderer() {
            return this.gameRenderer;
        }
        public float getPartialTicks() {
            return this.partialTicks;
        }
    }
}
