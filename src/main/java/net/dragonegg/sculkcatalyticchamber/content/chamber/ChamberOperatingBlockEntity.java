package net.dragonegg.sculkcatalyticchamber.content.chamber;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.simple.DeferralBehaviour;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ChamberOperatingBlockEntity extends KineticBlockEntity {

    public DeferralBehaviour chamberChecker;
    public boolean chamberRemoved;
    protected Recipe<?> currentRecipe;

    public ChamberOperatingBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        chamberChecker = new DeferralBehaviour(this, this::updateChamber);
        behaviours.add(chamberChecker);
    }

    @Override
    public void onSpeedChanged(float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        if (getSpeed() == 0)
            chamberRemoved = true;
        chamberRemoved = false;
        chamberChecker.scheduleUpdate();
    }

    @Override
    public void tick() {
        if (chamberRemoved) {
            chamberRemoved = false;
            onChamberRemoved();
            sendData();
            return;
        }

        super.tick();
    }

    protected boolean updateChamber() {
        if (getSpeed() == 0)
            return true;
        if (isRunning())
            return true;
        if (level == null || level.isClientSide)
            return true;
        Optional<ChamberBlockEntity> chamber = getChamber();
        if (chamber.filter(ChamberBlockEntity::canContinueProcessing).isEmpty())
            return true;

        List<Recipe<?>> recipes = getMatchingRecipes();
        if (recipes.isEmpty())
            return true;
        currentRecipe = recipes.get(0);
        startProcessingChamber();
        sendData();
        return true;
    }

    protected abstract boolean isRunning();

    public void startProcessingChamber() {}

    public boolean continueWithPreviousRecipe() {
        return true;
    }

    protected boolean matchChamberRecipe(Recipe<?> recipe) {
        if (recipe == null)
            return false;
        Optional<ChamberBlockEntity> chamber = getChamber();
        return chamber.filter(chamberBlockEntity ->
                ChamberRecipe.match(chamberBlockEntity, recipe)).isPresent();
    }

    protected void applyChamberRecipe() {
        if (currentRecipe == null)
            return;

        Optional<ChamberBlockEntity> optionalChamber = getChamber();
        if (optionalChamber.isEmpty())
            return;
        ChamberBlockEntity chamber = optionalChamber.get();
        boolean wasEmpty = chamber.canContinueProcessing();
        if (!ChamberRecipe.apply(chamber, currentRecipe))
            return;
        getProcessedRecipeTrigger().ifPresent(this::award);
        chamber.inputTank.sendDataImmediately();

        // Continue
        if (wasEmpty && matchChamberRecipe(currentRecipe)) {
            continueWithPreviousRecipe();
            sendData();
        }

        chamber.notifyChangeOfContents();
    }

    protected List<Recipe<?>> getMatchingRecipes() {
        Optional<ChamberBlockEntity> chamber = getChamber();
        if (chamber.map(ChamberBlockEntity::getTop).map(ChamberBlockEntity::isEmpty).orElse(true) &&
                chamber.map(ChamberBlockEntity::getMiddle).map(ChamberBlockEntity::isEmpty).orElse(true) &&
                chamber.map(ChamberBlockEntity::getBottom).map(ChamberBlockEntity::isEmpty).orElse(true))
            return new ArrayList<>();

        return getAllRecipes().stream()
                .filter(this::matchChamberRecipe)
                .sorted((r1, r2) ->
                        r2.getIngredients().size() - r1.getIngredients().size())
                .collect(Collectors.toList());
    }

    protected List<Recipe<?>> getAllRecipes() {
        return RecipeFinder.get(getRecipeCacheKey(), level, this::matchStaticFilters)
                .stream()
                .map(RecipeHolder::value)
                .collect(Collectors.toList());
    }

    protected abstract void onChamberRemoved();

    protected abstract Optional<ChamberBlockEntity> getChamber();

    protected Optional<CreateAdvancement> getProcessedRecipeTrigger() {
        return Optional.empty();
    }

    protected boolean matchStaticFilters(RecipeHolder<? extends Recipe<?>> holder) {
        return matchStaticFilters(holder.value());
    }

    protected abstract boolean matchStaticFilters(Recipe<?> recipe);

    protected abstract Object getRecipeCacheKey();

}
