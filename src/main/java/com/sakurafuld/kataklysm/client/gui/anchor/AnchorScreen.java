package com.sakurafuld.kataklysm.client.gui.anchor;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.sakurafuld.kataklysm.Kataklysm;
import com.sakurafuld.kataklysm.common.init.ModBlockEntities;
import com.sakurafuld.kataklysm.common.network.PacketHandler;
import com.sakurafuld.kataklysm.common.network.anchor.CS2SCAnchorUpdate;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.network.PacketDistributor;

public class AnchorScreen extends AbstractContainerScreen<AnchorMenu> {
    private static final ResourceLocation ANCHOR_LOCATION =
            new ResourceLocation(Kataklysm.ID, "textures/gui/container/anchor.png");
    private int frame;
    private String name = "";
    private TextFieldHelper field;

    public AnchorScreen(AnchorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.field = new TextFieldHelper(() -> this.name,s->{
            this.name = s;
        }
                ,
                TextFieldHelper.createClipboardGetter(this.minecraft),
                TextFieldHelper.createClipboardSetter(this.minecraft),
                (s) -> this.minecraft.font.width(s) <= 150);

    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.menu.level.getBlockEntity(this.menu.pos, ModBlockEntities.ANCHOR.get()).ifPresent(anchorBlockEntity -> {
            anchorBlockEntity.setName(this.name);
            PacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new CS2SCAnchorUpdate(this.menu.pos, this.name));
        });

//        PacketHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new C2SAnchorUpdate(this.tile.getBlockPos(), this.name));

        super.removed();
    }

    @Override
    protected void containerTick() {
        ++this.frame;
    }

    @Override
    public boolean charTyped(char c, int p_94684_) {
        this.field.charTyped(c);
        return super.charTyped(c, p_94684_);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifier) {
        if(key == 256 && this.shouldCloseOnEsc()){
            this.onClose();
            return true;
        }

        if (this.field.keyPressed(key)) return true;

        InputConstants.Key mouseKey = InputConstants.getKey(key, scanCode);
        boolean handled = this.checkHotbarKeyPressed(key, scanCode);// Forge MC-146650: Needs to return true when the key is handled
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            if (this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ClickType.CLONE);
                handled = true;
            } else if (this.minecraft.options.keyDrop.isActiveAndMatches(mouseKey)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, hasControlDown() ? 1 : 0, ClickType.THROW);
                handled = true;
            }
        } else if (this.minecraft.options.keyDrop.isActiveAndMatches(mouseKey)) {
            handled = true; // Forge MC-146650: Emulate MC bug, so we don't drop from hotbar when pressing drop without hovering over a item.
        }

        return handled;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ANCHOR_LOCATION);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderField(poseStack);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    public void renderField(PoseStack poseStack){
        boolean blink = this.frame / 6 % 2 == 0;
        int boardColor = FastColor.ARGB32.color(170, 0, 0, 0);
        int nameColor = FastColor.ARGB32.color(255, 255, 255, 255);
        int cursor = this.field.getCursorPos();
        int selection = this.field.getSelectionPos();
        poseStack.pushPose();
        poseStack.translate((double) (this.width / 2) - 0.45d, (double) (this.height / 2) - 60.0D, 50.0D);
        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
        if(this.name != null) {
            if(this.font.isBidirectional()) this.name = this.font.bidirectionalShaping(this.name);
            float halfWidth = ((float) -this.minecraft.font.width(this.name) / 2);
            fill(poseStack, (int) Math.floor(halfWidth - 5), -1, (int) Math.floor(-halfWidth + 5), 8, boardColor);
            this.minecraft.font.drawInBatch(this.name, halfWidth, 0, nameColor, false, matrix, bufferSource, false, 0, 15728880, false);
            if(cursor >= 0 && blink){
                int tail = this.minecraft.font.width(this.name.substring(0, Math.max(Math.min(cursor, this.name.length()), 0)))
                           - this.minecraft.font.width(this.name) / 2;
                if(this.name.isEmpty()){ tail -= 2; poseStack.translate(-0.6d, 0, 0);}
                if(cursor >= this.name.length()) this.minecraft.font.drawInBatch("_", tail, 0, nameColor, false, matrix, bufferSource, false, 0, 15728880, false);
                if(this.name.isEmpty()) poseStack.translate(0.6d, 0, 0);
            }
        }
        bufferSource.endBatch();
        if (this.name != null && cursor >= 0) {
            int j3 = this.minecraft.font.width(name.substring(0, Math.max(Math.min(cursor, name.length()), 0)));
            int k3 = j3 - this.minecraft.font.width(name) / 2;
            if (blink && cursor < name.length()) {
                fill(poseStack, k3, -1, k3 + 1, 9, nameColor);
            }
            if (selection != cursor) {
                int l3 = Math.min(cursor, selection);
                int l1 = Math.max(cursor, selection);
                int i2 = this.minecraft.font.width(this.name.substring(0, l3)) - this.minecraft.font.width(this.name) / 2;
                int j2 = this.minecraft.font.width(this.name.substring(0, l1)) - this.minecraft.font.width(this.name) / 2;
                int k2 = Math.min(i2, j2);
                int l2 = Math.max(i2, j2);
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.disableTexture();
                RenderSystem.enableColorLogicOp();
                RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(matrix, (float) k2, (float) 9, 0.0F).color(0, 0, 255, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) l2, (float) 9, 0.0F).color(0, 0, 255, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) l2, (float) 0, 0.0F).color(0, 0, 255, 255).endVertex();
                bufferbuilder.vertex(matrix, (float) k2, (float) 0, 0.0F).color(0, 0, 255, 255).endVertex();
                bufferbuilder.end();
                BufferUploader.end(bufferbuilder);
                RenderSystem.disableColorLogicOp();
                RenderSystem.enableTexture();
            }
        }
        poseStack.popPose();
    }
}
