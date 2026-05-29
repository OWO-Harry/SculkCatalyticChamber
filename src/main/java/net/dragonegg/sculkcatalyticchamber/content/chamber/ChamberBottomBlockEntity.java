package net.dragonegg.sculkcatalyticchamber.content.chamber;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import java.util.*;

import static net.dragonegg.sculkcatalyticchamber.content.chamber.ChamberBottomBlock.FACING;

public class ChamberBottomBlockEntity extends ChamberBlockEntity {

    protected SmartInventory outputInventory;
    protected SmartFluidTankBehaviour outputTank;
    protected FilteringBehaviour filtering;

    private Couple<SmartInventory> invs;
    private Couple<SmartFluidTankBehaviour> tanks;

    List<Direction> disabledSpoutputs;
    Direction preferredSpoutput;
    protected List<ItemStack> spoutputBuffer;
    protected List<FluidStack> spoutputFluidBuffer;

    public static final int OUTPUT_ANIMATION_TIME = 10;
    List<IntAttached<ItemStack>> visualizedOutputItems;
    List<IntAttached<FluidStack>> visualizedOutputFluids;

    public ChamberBottomBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        outputInventory = new ChamberInventory(9, this).forbidInsertion();
        outputInventory.whenContentsChanged($ -> contentsChanged = true);
        invs = Couple.create(inputInventory, outputInventory);
        tanks = Couple.create(inputTank, outputTank);
        visualizedOutputItems = Collections.synchronizedList(new ArrayList<>());
        visualizedOutputFluids = Collections.synchronizedList(new ArrayList<>());
        disabledSpoutputs = new ArrayList<>();
        preferredSpoutput = null;
        spoutputBuffer = new ArrayList<>();
        spoutputFluidBuffer = new ArrayList<>();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        filtering = new FilteringBehaviour(this, new ChamberValueBox())
                .withCallback(newFilter -> contentsChanged = true).forRecipes();
        behaviours.add(filtering);

        outputTank = new SmartFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 2, 8000, true)
                .whenFluidUpdates(() -> contentsChanged = true).forbidInsertion();
        behaviours.add(outputTank);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        outputInventory.deserializeNBT(registries, compound.getCompound("OutputItems"));

        preferredSpoutput = null;
        if (compound.contains("PreferredSpoutput"))
            preferredSpoutput = NBTHelper.readEnum(compound, "PreferredSpoutput", Direction.class);
        disabledSpoutputs.clear();
        ListTag disabledList = compound.getList("DisabledSpoutput", Tag.TAG_STRING);
        disabledList.forEach(d -> disabledSpoutputs.add(Direction.valueOf(((StringTag) d).getAsString())));
        spoutputBuffer = NBTHelper.readItemList(compound.getList("Overflow", Tag.TAG_COMPOUND), registries);
        spoutputFluidBuffer = new ArrayList<>();

        if (!clientPacket)
            return;

        visualizedOutputItems.clear();
        visualizedOutputFluids.clear();
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.put("OutputItems", outputInventory.serializeNBT(registries));

        if (preferredSpoutput != null)
            NBTHelper.writeEnum(compound, "PreferredSpoutput", preferredSpoutput);
        ListTag disabledList = new ListTag();
        disabledSpoutputs.forEach(d -> disabledList.add(StringTag.valueOf(d.name())));
        compound.put("DisabledSpoutput", disabledList);
        compound.put("Overflow", NBTHelper.writeItemList(spoutputBuffer, registries));
        compound.put("FluidOverflow", new ListTag());

        if (!clientPacket)
            return;

        compound.put("VisualizedItems", new ListTag());
        compound.put("VisualizedFluids", new ListTag());
        visualizedOutputItems.clear();
        visualizedOutputFluids.clear();
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, outputInventory);
        spoutputBuffer.forEach(is -> Block.popResource(level, worldPosition, is));
    }

    @Override
    public boolean isEmpty() {
        return inputInventory.isEmpty() && outputInventory.isEmpty() && inputTank.isEmpty() && outputTank.isEmpty();
    }

    public boolean isBufferEmpty() {
        return spoutputBuffer.isEmpty() && spoutputFluidBuffer.isEmpty();
    }

    public void onWrenched(Direction face) {
        BlockState blockState = getBlockState();
        Direction currentFacing = blockState.getValue(FACING);

        disabledSpoutputs.remove(face);
        if (currentFacing == face) {
            if (preferredSpoutput == face)
                preferredSpoutput = null;
            disabledSpoutputs.add(face);
        } else
            preferredSpoutput = face;

        updateSpoutput();
    }

    @Override
    public void lazyTick() {
        if (!level.isClientSide) {
            updateSpoutput();
        }
        super.lazyTick();
    }

    private void updateSpoutput() {
        BlockState blockState = getBlockState();
        Direction currentFacing = blockState.getValue(FACING);
        Direction newFacing = Direction.DOWN;
        for (Direction test : Iterate.horizontalDirections) {
            boolean canOutputTo = BasinBlock.canOutputTo(level, worldPosition, test);
            if (canOutputTo && !disabledSpoutputs.contains(test)) newFacing = test;
        }

        if (preferredSpoutput != null && BasinBlock.canOutputTo(level, worldPosition, preferredSpoutput)
                && preferredSpoutput != Direction.UP)
            newFacing = preferredSpoutput;

        if (newFacing == currentFacing) return;

        level.setBlockAndUpdate(worldPosition, blockState.setValue(FACING, newFacing));

        if (newFacing.getAxis().isVertical()) return;

        for (int slot = 0; slot < outputInventory.getSlots(); slot++) {
            ItemStack extractItem = outputInventory.extractItem(slot, 64, true);
            if (extractItem.isEmpty())
                continue;
            if (acceptOutputs(ImmutableList.of(extractItem), Collections.emptyList(), true))
                acceptOutputs(ImmutableList.of(outputInventory.extractItem(slot, 64, false)),
                        Collections.emptyList(), false);
        }

        IFluidHandler handler = outputTank.getCapability();
        for (int slot = 0; slot < handler.getTanks(); slot++) {
            FluidStack fs = handler.getFluidInTank(slot).copy();
            if (fs.isEmpty())
                continue;
            if (acceptOutputs(Collections.emptyList(), ImmutableList.of(fs), true)) {
                handler.drain(fs, IFluidHandler.FluidAction.EXECUTE);
                acceptOutputs(Collections.emptyList(), ImmutableList.of(fs), false);
            }
        }

        notifyChangeOfContents();
        notifyUpdate();
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            createFluidParticles();
            tickVisualizedOutputs();
        }

        if ((!spoutputBuffer.isEmpty() || !spoutputFluidBuffer.isEmpty()) && !level.isClientSide)
            tryClearingSpoutputOverflow();

        scheduleChangeOfContents();

    }

    private void tryClearingSpoutputOverflow() {
        BlockState blockState = getBlockState();
        Direction direction = blockState.getValue(FACING);
        BlockEntity be = level.getBlockEntity(worldPosition.below().relative(direction));

        FilteringBehaviour filter = null;
        InvManipulationBehaviour inserter = null;
        if (be != null) {
            filter = BlockEntityBehaviour.get(level, be.getBlockPos(), FilteringBehaviour.TYPE);
            inserter = BlockEntityBehaviour.get(level, be.getBlockPos(), InvManipulationBehaviour.TYPE);
        }

        if (filter != null && filter.isRecipeFilter())
            filter = null; // Do not test spout outputs against the recipe filter

        IItemHandler targetInv = be == null ? null
                : Optional.ofNullable(level.getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), be.getBlockState(), be, direction.getOpposite()))
                .orElse(inserter == null ? null : inserter.getInventory());

        IFluidHandler targetTank = be == null ? null
                : level.getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), be.getBlockState(), be, direction.getOpposite());

        boolean update = false;

        for (Iterator<ItemStack> iterator = spoutputBuffer.iterator(); iterator.hasNext();) {
            ItemStack itemStack = iterator.next();

            if (direction == Direction.DOWN) {
                Block.popResource(level, worldPosition, itemStack);
                iterator.remove();
                update = true;
                continue;
            }

            if (targetInv == null)
                break;

            ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetInv, itemStack, true);
            if (remainder.getCount() == itemStack.getCount())
                continue;
            if (filter != null && !filter.test(itemStack))
                continue;

            if (visualizedOutputItems.size() < 3)
                visualizedOutputItems.add(IntAttached.withZero(itemStack));
            update = true;

            remainder = ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), false);
            if (remainder.isEmpty())
                iterator.remove();
            else
                itemStack.setCount(remainder.getCount());
        }

        for (Iterator<FluidStack> iterator = spoutputFluidBuffer.iterator(); iterator.hasNext();) {
            FluidStack fluidStack = iterator.next();

            if (direction == Direction.DOWN) {
                iterator.remove();
                update = true;
                continue;
            }

            if (targetTank == null)
                break;

            for (boolean simulate : Iterate.trueAndFalse) {
                IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
                int fill = targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler
                        ? ((SmartFluidTankBehaviour.InternalFluidHandler) targetTank).forceFill(fluidStack.copy(), action)
                        : targetTank.fill(fluidStack.copy(), action);
                if (fill != fluidStack.getAmount())
                    break;
                if (simulate)
                    continue;

                update = true;
                iterator.remove();
                if (visualizedOutputFluids.size() < 3)
                    visualizedOutputFluids.add(IntAttached.withZero(fluidStack));
            }
        }

        if (update) {
            notifyChangeOfContents();
            sendData();
        }
    }

    @Override
    public Optional<ChamberOperatingBlockEntity> getOperator() {
        if (level == null) return Optional.empty();
        BlockEntity be = level.getBlockEntity(worldPosition.above(4));
        if (be instanceof ChamberOperatingBlockEntity operator)
            return Optional.of(operator);
        return Optional.empty();
    }

    public SmartInventory getOutputInventory() {
        return outputInventory;
    }

    @Override
    public IItemHandlerModifiable getInvs() {
        return new CombinedInvWrapper(inputInventory, outputInventory);
    }

    @Override
    public IFluidHandler getTanks() {
        IFluidHandler inputCap = inputTank.getCapability();
        IFluidHandler outputCap = outputTank.getCapability();
        return new CombinedTankWrapper(outputCap, inputCap);
    }

    @Override
    public ChamberTopBlockEntity getTop() {
        BlockEntity be = level.getBlockEntity(getBlockPos().above(2));
        if (be instanceof ChamberTopBlockEntity top) {
            return top;
        }
        return null;
    }

    @Override
    public ChamberMiddleBlockEntity getMiddle() {
        BlockEntity be = level.getBlockEntity(getBlockPos().above(1));
        if (be instanceof ChamberMiddleBlockEntity middle) {
            return middle;
        }
        return null;
    }

    @Override
    public ChamberBottomBlockEntity getBottom() {
        return this;
    }

    @Override
    protected NonNullList<Ingredient> ingredients(Recipe<?> recipe) {
        if (recipe instanceof ChamberRecipe chamberRecipe)
            return chamberRecipe.bottomIngredients;
        return NonNullList.create();
    }

    public boolean acceptOutputs(List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean simulate) {
        outputInventory.allowInsertion();
        outputTank.allowInsertion();
        boolean acceptOutputsInner = acceptOutputsInner(outputItems, outputFluids, simulate);
        outputInventory.forbidInsertion();
        outputTank.forbidInsertion();
        return acceptOutputsInner;
    }

    private boolean acceptOutputsInner(List<ItemStack> outputItems, List<FluidStack> outputFluids, boolean simulate) {
        BlockState blockState = getBlockState();

        Direction direction = blockState.getValue(FACING);
        if (direction != Direction.DOWN) {

            BlockEntity be = level.getBlockEntity(worldPosition.below().relative(direction));

            InvManipulationBehaviour inserter = be == null ? null :
                    BlockEntityBehaviour.get(level, be.getBlockPos(), InvManipulationBehaviour.TYPE);
            IItemHandler targetInv = be == null ? null :
                    Optional.ofNullable(level.getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), be.getBlockState(), be, direction.getOpposite()))
                            .orElse(inserter == null ? null : inserter.getInventory());
            IFluidHandler targetTank = be == null ? null
                    : level.getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), be.getBlockState(), be, direction.getOpposite());
            boolean externalTankNotPresent = targetTank == null;

            if (!outputItems.isEmpty() && targetInv == null)
                return false;
            if (!outputFluids.isEmpty() && externalTankNotPresent) {
                // Special case - fluid outputs but output only accepts items
                targetTank = outputTank.getCapability();
                if (targetTank == null)
                    return false;
                if (!acceptFluidOutputsIntoChamber(outputFluids, simulate, targetTank))
                    return false;
            }

            if (simulate)
                return true;
            for (ItemStack itemStack : outputItems)
                if (!itemStack.isEmpty())
                    spoutputBuffer.add(itemStack.copy());
            if (!externalTankNotPresent)
                for (FluidStack fluidStack : outputFluids)
                    spoutputFluidBuffer.add(fluidStack.copy());
            return true;
        }

        IItemHandler targetInv = outputInventory;
        IFluidHandler targetTank = outputTank.getCapability();

        if (targetInv == null && !outputItems.isEmpty())
            return false;
        if (!acceptItemOutputsIntoChamber(outputItems, simulate, targetInv))
            return false;
        if (outputFluids.isEmpty())
            return true;
        if (targetTank == null)
            return false;
        if (!acceptFluidOutputsIntoChamber(outputFluids, simulate, targetTank))
            return false;

        return true;
    }

    private boolean acceptFluidOutputsIntoChamber(List<FluidStack> outputFluids, boolean simulate,
                                                  IFluidHandler targetTank) {
        for (FluidStack fluidStack : outputFluids) {
            IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
            int fill = targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler
                    ? ((SmartFluidTankBehaviour.InternalFluidHandler) targetTank).forceFill(fluidStack.copy(), action)
                    : targetTank.fill(fluidStack.copy(), action);
            if (fill != fluidStack.getAmount())
                return false;
        }
        return true;
    }

    private boolean acceptItemOutputsIntoChamber(List<ItemStack> outputItems, boolean simulate,
                                                 IItemHandler targetInv) {
        for (ItemStack itemStack : outputItems) {
            if (!ItemHandlerHelper.insertItemStacked(targetInv, itemStack.copy(), simulate).isEmpty())
                return false;
        }
        return true;
    }

    protected InteractionResult use(Level worldIn, BlockPos pos, Player player, InteractionHand handIn) {
        ItemStack heldItem = player.getItemInHand(handIn);
        SmartInventory inv = getOutputInventory();
        if (heldItem.isEmpty() && !inv.isEmpty()) {
            if (emptyInv(inv, player)) {
                worldIn.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F + Create.RANDOM.nextFloat());
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(worldIn, pos, player, handIn);
    }

    private void tickVisualizedOutputs() {
        visualizedOutputFluids.forEach(IntAttached::decrement);
        visualizedOutputItems.forEach(IntAttached::decrement);
        visualizedOutputFluids.removeIf(IntAttached::isOrBelowZero);
        visualizedOutputItems.removeIf(IntAttached::isOrBelowZero);
    }

    private void createFluidParticles() {
        RandomSource r = level.random;
        BlockState blockState = getBlockState();
        Direction direction = blockState.getValue(FACING);
        if (direction == Direction.DOWN)
            return;
        Vec3 directionVec = Vec3.atLowerCornerOf(direction.getNormal());
        Vec3 outVec = VecHelper.getCenterOf(worldPosition)
                .add(directionVec.scale(.65)
                        .subtract(0, 1 / 4f, 0));
        Vec3 outMotion = directionVec.scale(1 / 16f)
                .add(0, -1 / 16f, 0);

        for (int i = 0; i < 2; i++) {
            visualizedOutputFluids.forEach(ia -> {
                FluidStack fluidStack = ia.getValue();
                ParticleOptions fluidParticle = FluidFX.getFluidParticle(fluidStack);
                Vec3 m = VecHelper.offsetRandomly(outMotion, r, 1 / 16f);
                level.addAlwaysVisibleParticle(fluidParticle, outVec.x, outVec.y, outVec.z, m.x, m.y, m.z);
            });
        }
    }

    class ChamberValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 16.05);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis()
                    .isHorizontal();
        }

    }

}
