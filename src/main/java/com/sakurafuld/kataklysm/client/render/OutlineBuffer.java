package com.sakurafuld.kataklysm.client.render;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

//This is free and unencumbered software released into the public domain.
//だそうなので.
public class OutlineBuffer implements MultiBufferSource {

    public static final OutlineBuffer INSTANCE = new OutlineBuffer();

    private OutlineBuffer() {}

    @NotNull
    @Override
    public VertexConsumer getBuffer(@NotNull RenderType type) {
        return Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(OutlineRenderType.get(type));
    }
}