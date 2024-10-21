package com.sakurafuld.kataklysm.api.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

//This is free and unencumbered software released into the public domain.
//らしいのでつかわせてもらいます！！.
@OnlyIn(Dist.CLIENT)
public class OutlineRenderType extends RenderType {

    private static final Map<RenderType, OutlineRenderType> TYPES = new HashMap<>();

    private final RenderType parent;

    private OutlineRenderType(RenderType parent) {
        super("Outline" + parent.toString(), parent.format(), parent.mode(), parent.bufferSize(), parent.affectsCrumbling(), true,
                parent::setupRenderState, parent::clearRenderState);
        this.parent = parent;
    }

    public static RenderType get(RenderType parent) {
        if (parent.toString().contains("glint")) {
            return parent;
        } else if (parent instanceof OutlineRenderType) {
            return parent;
        } else {
            if (!TYPES.containsKey(parent)) {
                TYPES.put(parent, new OutlineRenderType(parent));
            }
            return TYPES.get(parent);
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "Outline" + this.parent;
    }

    @Override
    public void setupRenderState() {
        this.parent.setupRenderState();
        if (Minecraft.getInstance().levelRenderer.entityTarget() != null)
            Minecraft.getInstance().levelRenderer.entityTarget().bindWrite(false);

    }

    @Override
    public void clearRenderState() {
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        this.parent.clearRenderState();
    }
}