package com.sakurafuld.kataklysm.client.gui.anchor;

import com.sakurafuld.kataklysm.common.init.ModBlockEntities;
import com.sakurafuld.kataklysm.common.init.ModBlocks;
import com.sakurafuld.kataklysm.common.init.ModMenus;
import com.sakurafuld.kataklysm.common.network.PacketHandler;
import com.sakurafuld.kataklysm.common.network.anchor.CS2SCAnchorUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class AnchorMenu extends AbstractContainerMenu {
    public final Level level;
    public final BlockPos pos;
    private final Container icon;
    public AnchorMenu(int id, Inventory playerInv, FriendlyByteBuf buffer) {
        this(id, playerInv, buffer.readBlockPos());
    }
    public AnchorMenu(int id, Inventory playerInv, BlockPos pos){
        super(ModMenus.ANCHOR.get(), id);
        this.level = playerInv.player.level;
        this.pos = pos;
        this.addSlot(new Slot(this.icon = new SimpleContainer(1), 0, 80, 22));
        for(int row = 0; row < 3; ++row) {
            for(int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInv, column + row * 9 + 9, 8 + column * 18, row * 18 + 51));
            }
        }

        for(int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInv, column, 8 + column * 18, 109));
        }
    }

    @Override
    public void removed(Player player) {
        if(this.level != null && !this.level.isClientSide){
            this.level.getBlockEntity(this.pos, ModBlockEntities.ANCHOR.get()).ifPresent(anchorBlockEntity -> {
                anchorBlockEntity.setIcon(this.slots.get(0).getItem().copy());
                anchorBlockEntity.setChanged();
            });
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()-> (ServerPlayer) player), new CS2SCAnchorUpdate(this.pos, this.slots.get(0).getItem().copy()));

        }
        this.clearContainer(player, this.icon);
        super.removed(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(this.level, this.pos), player, ModBlocks.ANCHOR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot srcSlot = this.slots.get(index);
        if(srcSlot == null || !srcSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack srcStack = srcSlot.getItem();
        ItemStack retStack = srcStack.copy();
        int ANCHOR_SIZE = 1;
        if(index < ANCHOR_SIZE){
            if(!this.moveItemStackTo(srcStack, ANCHOR_SIZE, this.slots.size(), true))
                return ItemStack.EMPTY;
        }else if(!this.moveItemStackTo(srcStack, 0, ANCHOR_SIZE, false))
            return ItemStack.EMPTY;

        if(srcStack.isEmpty())
            srcSlot.set(ItemStack.EMPTY);
        else
            srcSlot.setChanged();

        return retStack;
    }


}
