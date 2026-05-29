package net.dragonegg.sculkcatalyticchamber.content.chamber;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class ChamberMiddleBlockEntity extends ChamberBlockEntity {

    public ChamberMiddleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        scheduleChangeOfContents();
    }

    @Override
    public Optional<ChamberOperatingBlockEntity> getOperator() {
        if (level == null) return Optional.empty();
        BlockEntity be = level.getBlockEntity(worldPosition.above(3));
        if (be instanceof ChamberOperatingBlockEntity operator)
            return Optional.of(operator);
        return Optional.empty();
    }

    @Override
    public ChamberTopBlockEntity getTop() {
        BlockEntity be = level.getBlockEntity(getBlockPos().above(1));
        if (be instanceof ChamberTopBlockEntity top) {
            return top;
        }
        return null;
    }

    @Override
    public ChamberMiddleBlockEntity getMiddle() {
        return this;
    }

    @Override
    public ChamberBottomBlockEntity getBottom() {
        BlockEntity be = level.getBlockEntity(getBlockPos().below(1));
        if (be instanceof ChamberBottomBlockEntity bottom) {
            return bottom;
        }
        return null;
    }

    @Override
    protected NonNullList<Ingredient> ingredients(Recipe<?> recipe) {
        if (recipe instanceof ChamberRecipe chamberRecipe)
            return chamberRecipe.catalysts;
        return NonNullList.create();
    }


}
