package net.dragonegg.sculkcatalyticchamber.content.block.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class TopBlockEntity extends KineticBlockEntity {

    //TODO: use config value
    private static final float REQ_STRESS = 1;

    public SmartInventory inv;
    public SmartFluidTankBehaviour tank;
    protected LazyOptional<IItemHandlerModifiable> itemCapability;
    protected LazyOptional<IFluidHandler> fluidCapability;

    public TopBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.inv = new SmartInventory(9, this);
        this.itemCapability = LazyOptional.of(() -> this.inv);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this));
        this.tank = (new SmartFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 2, 1000, true));
        behaviours.add(this.tank);
        this.fluidCapability = LazyOptional.of(() -> {
            LazyOptional<? extends IFluidHandler> inputCap = this.tank.getCapability();
            return inputCap.orElse(null);
        });
    }

    @Override
    public float calculateStressApplied() {
        this.lastStressApplied = REQ_STRESS;
        return REQ_STRESS;
    }

    @Override
    public float calculateAddedStressCapacity() {
        return 0;
    }

    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        this.inv.deserializeNBT(compound.getCompound("InputItems"));
    }

    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.put("InputItems", this.inv.serializeNBT());
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(this.level, this.worldPosition, this.inv);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.itemCapability.invalidate();
        this.fluidCapability.invalidate();
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return this.itemCapability.cast();
        } else {
            return cap == ForgeCapabilities.FLUID_HANDLER ? this.fluidCapability.cast() : super.getCapability(cap, side);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        Lang.translate("gui.goggles.basin_contents").forGoggles(tooltip);
        IItemHandlerModifiable items = this.itemCapability.orElse(new ItemStackHandler());
        IFluidHandler fluids = this.fluidCapability.orElse(new FluidTank(0));
        boolean isEmpty = true;

        for(int i = 0; i < items.getSlots(); ++i) {
            ItemStack stackInSlot = items.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                Lang.text("").add(Components.translatable(stackInSlot.getDescriptionId()).withStyle(ChatFormatting.GRAY)).add(Lang.text(" x" + stackInSlot.getCount()).style(ChatFormatting.GREEN)).forGoggles(tooltip, 1);
                isEmpty = false;
            }
        }

        LangBuilder mb = Lang.translate("generic.unit.millibuckets");

        for(int i = 0; i < fluids.getTanks(); ++i) {
            FluidStack fluidStack = fluids.getFluidInTank(i);
            if (!fluidStack.isEmpty()) {
                Lang.text("").add(Lang.fluidName(fluidStack).add(Lang.text(" ")).style(ChatFormatting.GRAY).add(Lang.number(fluidStack.getAmount()).add(mb).style(ChatFormatting.BLUE))).forGoggles(tooltip, 1);
                isEmpty = false;
            }
        }

        if (isEmpty) {
            tooltip.remove(0);
        }

        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    public SmartInventory getInventory() {
        return this.inv;
    }
}
