package com.sakurafuld.kataklysm.common.block.anchor;

import com.sakurafuld.kataklysm.client.render.block.AnchorBlockEntityRenderer;
import com.sakurafuld.kataklysm.common.init.ModBlockEntities;
import com.sakurafuld.kataklysm.common.network.PacketHandler;
import com.sakurafuld.kataklysm.common.network.anchor.S2CAnchorRemove;
import mekanism.api.gear.IModule;
import mekanism.common.content.gear.mekatool.ModuleTeleportationUnit;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.registries.MekanismModules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class AnchorBlock extends Block implements EntityBlock {
    public AnchorBlock(BlockBehaviour.Properties prop){
        super(prop);
    }


    public static boolean isAnchoringItem(ItemStack stack){
        if (!(ModList.get().isLoaded("mekanism") && stack.getItem() instanceof ItemMekaTool mt))
            return false;

        IModule<ModuleTeleportationUnit> unit = mt.getModule(stack, MekanismModules.TELEPORTATION_UNIT);
        return unit != null && unit.isEnabled();

    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext ctx) {

        return Block.box(0, 0, 0, 16, 13, 16);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if(entity instanceof ServerPlayer player) NetworkHooks.openGui(player, ((AnchorBlockEntity) level.getBlockEntity(pos)), pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newstate, boolean p_60519_) {
        AnchorBlockEntityRenderer.getAnchors().remove(pos);
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new S2CAnchorRemove(pos));
        super.onRemove(state, level, pos, newstate, p_60519_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.ANCHOR.get().create(pos, state);
    }

}
