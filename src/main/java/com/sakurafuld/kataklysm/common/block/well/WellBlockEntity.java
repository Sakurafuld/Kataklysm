package com.sakurafuld.kataklysm.common.block.well;

import com.sakurafuld.kataklysm.common.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WellBlockEntity extends BlockEntity {
    private static final Capability<IFluidHandler> FLUID_HANDLER = CapabilityManager.get(new CapabilityToken<>(){});
    private static final LazyOptional<IFluidHandler> INFINITY = LazyOptional.of(()-> new IFluidHandler(){

        @Override
        public int getTanks() {
            return 1;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return new FluidStack(Fluids.WATER, Integer.MAX_VALUE);
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return stack.getFluid() == Fluids.WATER;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return resource.getFluid() == Fluids.WATER ? new FluidStack(Fluids.WATER, resource.getAmount()) : FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return new FluidStack(Fluids.WATER, maxDrain);
        }
    });
    private static final LazyOptional<IFluidHandler> VOID = LazyOptional.of(()-> new IFluidHandler(){

        @Override
        public int getTanks() {
            return 1;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return resource.getAmount();
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    });
    public WellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WELL.get(), pos, state);
    }
    public static void tick(@NotNull Level level, BlockPos pos, BlockState state, BlockEntity be){
        if(level.isClientSide) return;
        fill:{
            BlockPos at = pos.relative(state.getValue(HopperBlock.FACING));
            if (!(level.getBlockState(at).getBlock() instanceof EntityBlock)) break fill;
            level.getBlockEntity(at).getCapability(FLUID_HANDLER, state.getValue(HopperBlock.FACING).getOpposite())
                    .ifPresent(c ->
                            c.fill(new FluidStack(Fluids.WATER, Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE));
        }
        if(!(state.getValue(HopperBlock.ENABLED))) return;
        if(!(level.getBlockState(pos.relative(Direction.UP)).getBlock() instanceof EntityBlock)) return;
        level.getBlockEntity(pos.relative(Direction.UP)).getCapability(FLUID_HANDLER, Direction.DOWN)
                .ifPresent(c ->
                        c.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == FLUID_HANDLER){
            if(side == Direction.UP)
                return this.getBlockState().getValue(HopperBlock.ENABLED) ?
                        VOID.cast() : super.getCapability(cap, side);
            return INFINITY.cast();
        }
        return super.getCapability(cap, side);
    }
}
