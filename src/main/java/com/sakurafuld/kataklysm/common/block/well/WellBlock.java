package com.sakurafuld.kataklysm.common.block.well;

import com.sakurafuld.kataklysm.common.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.HopperBlock.ENABLED;
import static net.minecraft.world.level.block.HopperBlock.FACING;

public class WellBlock extends Block implements EntityBlock {
    private static final VoxelShape TOP = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape FUNNEL = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
    private static final VoxelShape CONVEX_BASE = Shapes.or(FUNNEL, TOP);
    private static final VoxelShape BASE = Shapes.join(CONVEX_BASE, Hopper.INSIDE, BooleanOp.ONLY_FIRST);
    private static final VoxelShape DOWN_SHAPE = Shapes.or(BASE, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
    private static final VoxelShape EAST_SHAPE = Shapes.or(BASE, Block.box(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
    private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
    private static final VoxelShape WEST_SHAPE = Shapes.or(BASE, Block.box(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
    private static final VoxelShape DOWN_INTERACTION_SHAPE = Hopper.INSIDE;
    private static final VoxelShape EAST_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
    private static final VoxelShape NORTH_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
    private static final VoxelShape SOUTH_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
    private static final VoxelShape WEST_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));

    public WellBlock(Properties prop) {
        super(prop);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(ENABLED, Boolean.TRUE));
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_54106_, BlockPos p_54107_, CollisionContext p_54108_) {
        return switch (state.getValue(FACING)) {
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> BASE;
        };
    }
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter p_54100_, BlockPos p_54101_) {
        return switch (state.getValue(FACING)) {
            case DOWN -> DOWN_INTERACTION_SHAPE;
            case NORTH -> NORTH_INTERACTION_SHAPE;
            case SOUTH -> SOUTH_INTERACTION_SHAPE;
            case WEST -> WEST_INTERACTION_SHAPE;
            case EAST -> EAST_INTERACTION_SHAPE;
            default -> Hopper.INSIDE;
        };
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_54041_) {
        Direction direction = p_54041_.getClickedFace().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction).setValue(ENABLED, Boolean.TRUE);
    }
    @Override
    public void onPlace(BlockState p_54110_, Level p_54111_, BlockPos p_54112_, BlockState p_54113_, boolean p_54114_) {
        if (!p_54113_.is(p_54110_.getBlock())) {
            this.checkPoweredState(p_54111_, p_54112_, p_54110_);
        }
    }
    @Override
    public void neighborChanged(BlockState p_54078_, Level p_54079_, BlockPos p_54080_, Block p_54081_, BlockPos p_54082_, boolean p_54083_) {
        this.checkPoweredState(p_54079_, p_54080_, p_54078_);
    }

    private void checkPoweredState(Level level, BlockPos p_54046_, BlockState p_54047_) {
        boolean flag = !level.hasNeighborSignal(p_54046_);
        if (flag != p_54047_.getValue(ENABLED)) {
            level.setBlockAndUpdate(p_54046_, p_54047_.setValue(ENABLED, flag));
        }

    }
    @Override
    public RenderShape getRenderShape(BlockState p_54103_) {
        return RenderShape.MODEL;
    }
    @Override
    public BlockState rotate(BlockState p_54094_, Rotation p_54095_) {
        return p_54094_.setValue(FACING, p_54095_.rotate(p_54094_.getValue(FACING)));
    }
    @Override
    public BlockState mirror(BlockState p_54091_, Mirror p_54092_) {
        return p_54091_.rotate(p_54092_.getRotation(p_54091_.getValue(FACING)));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_54097_) {
        p_54097_.add(FACING, ENABLED);
    }
    @Override
    public boolean isPathfindable(BlockState p_54057_, BlockGetter p_54058_, BlockPos p_54059_, PathComputationType p_54060_) {
        return false;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.WELL.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> type) {
        return type == ModBlockEntities.WELL.get() ? WellBlockEntity::tick : null;
    }
}