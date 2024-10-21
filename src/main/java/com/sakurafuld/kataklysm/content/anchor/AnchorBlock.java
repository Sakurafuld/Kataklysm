package com.sakurafuld.kataklysm.content.anchor;

import com.sakurafuld.kataklysm.content.ModBlockEntities;
import com.sakurafuld.kataklysm.network.PacketHandler;
import com.sakurafuld.kataklysm.network.anchor.ClientboundAnchorRemove;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static com.sakurafuld.kataklysm.Deets.*;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class AnchorBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {

    public AnchorBlock(Properties prop){
        super(prop);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    public boolean canTeleport(Player player) {
        Predicate<ItemStack> PREDICATE
                = stack -> stack.getItem() instanceof ItemMekaTool mt && mt.getModule(stack, MekanismModules.TELEPORTATION_UNIT) != null && mt.getModule(stack, MekanismModules.TELEPORTATION_UNIT).isEnabled();

        return player.isShiftKeyDown() && PREDICATE.test(player.getItemInHand(InteractionHand.MAIN_HAND)) || PREDICATE.test(player.getItemInHand(InteractionHand.OFF_HAND));
    }
    public boolean drainEnergy(Player player, double distance, ItemStack stack){
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        FloatingLong energyNeeded = MekanismConfig.gear.mekaToolEnergyUsageTeleport.get().multiply(distance / 20);
        if (energyContainer == null || (!player.getAbilities().instabuild && energyContainer.getEnergy().smallerThan(energyNeeded))){
            return false;
        }
        if(!player.getAbilities().instabuild)
            energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.MANUAL);

        return true;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext ctx) {

        return Block.box(0, 0, 0, 16, 13, 16);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {

        return this.defaultBlockState().setValue(WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).getType() == Fluids.WATER);
    }
    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WATERLOGGED);
    }
    @Override
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if(entity instanceof ServerPlayer player) NetworkHooks.openGui(player, ((AnchorBlockEntity) level.getBlockEntity(pos)), pos);
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newstate, boolean pIsMoving) {
        LOG.debug("onRemove is {}-side", side());
        AnchorHandler.removeAnchor(pos);
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundAnchorRemove(pos));
        super.onRemove(state, level, pos, newstate, pIsMoving);
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.ANCHOR.get().create(pos, state);
    }
}
