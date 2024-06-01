package com.sakurafuld.kataklysm.common.block.anchor;

import com.sakurafuld.kataklysm.client.gui.anchor.AnchorMenu;
import com.sakurafuld.kataklysm.client.render.block.AnchorBlockEntityRenderer;
import com.sakurafuld.kataklysm.common.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AnchorBlockEntity extends BlockEntity implements MenuProvider {
    private String name = "";
    private ItemStack icon = ItemStack.EMPTY;
    public AnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANCHOR.get(), pos, state);
    }
    @Override
    public void onLoad() {
        AnchorBlockEntityRenderer.getAnchors().add(this.worldPosition);
        super.onLoad();
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
    public ItemStack getIcon() {
        return icon;
    }
    public void setIcon(ItemStack icon){
        this.icon = icon;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        CompoundTag anchor = new CompoundTag();
        this.icon.save(anchor);
        anchor.putString("AnchorName", this.name);
        tag.put("Anchor", anchor);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        CompoundTag anchor = tag.getCompound("Anchor");
        this.name = anchor.getString("AnchorName");
        this.icon = ItemStack.of(anchor);
    }
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag anchor = new CompoundTag();
        anchor.putString("AnchorName", this.name);
        this.icon.save(anchor);
        tag.put("Anchor", anchor);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.kataklysm.anchor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory plyerInv, Player player) {
        return new AnchorMenu(id, plyerInv, this.worldPosition);
    }
}
