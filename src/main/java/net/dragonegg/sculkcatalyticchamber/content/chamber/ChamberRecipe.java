package net.dragonegg.sculkcatalyticchamber.content.chamber;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.Iterate;
import net.dragonegg.sculkcatalyticchamber.registry.RecipeRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.getHeatLevelOf;

public class ChamberRecipe implements Recipe<SmartInventory> {

    private static final Random r = new Random();

    protected ResourceLocation id;
    protected NonNullList<Ingredient> topIngredients;
    protected NonNullList<FluidIngredient> topFluidIngredients;
    protected NonNullList<Ingredient> bottomIngredients;
    protected NonNullList<FluidIngredient> bottomFluidIngredients;
    protected NonNullList<Ingredient> catalysts;
    protected NonNullList<FluidIngredient> fluidCatalysts;
    protected double chances;
    protected NonNullList<ProcessingOutput> results;
    protected NonNullList<FluidStack> fluidResults;
//    protected int processingDuration;
    protected HeatCondition requiredHeat;

    private RecipeType<?> type;
    private RecipeSerializer<?> serializer;
    private IRecipeTypeInfo typeInfo;
    private Supplier<ItemStack> forcedResult;

    protected ChamberRecipe(IRecipeTypeInfo typeInfo, ChamberRecipeBuilder.ChamberRecipeParams params) {
        this.id = params.id;
        this.topIngredients = params.topIngredients;
        this.topFluidIngredients = params.topFluidIngredients;
        this.bottomIngredients = params.bottomIngredients;
        this.bottomFluidIngredients = params.bottomFluidIngredients;
        this.catalysts = params.catalysts;
        this.fluidCatalysts = params.fluidCatalysts;
        this.chances = params.chances;
        this.results = params.results;
        this.fluidResults = params.fluidResults;
//        this.processingDuration = params.processingDuration;
        this.requiredHeat = params.requiredHeat;

        this.type = typeInfo.getType();
        this.serializer = typeInfo.getSerializer();
        this.typeInfo = typeInfo;
        this.forcedResult = null;
    }

    public ChamberRecipe(ChamberRecipeBuilder.ChamberRecipeParams params) {
        this(RecipeRegistry.CHAMBER, params);
    }

    public NonNullList<Ingredient> getTopIngredients() {
        return topIngredients;
    }

    public NonNullList<Ingredient> getCatalysts() {
        return catalysts;
    }

    public NonNullList<Ingredient> getBottomIngredients() {
        return bottomIngredients;
    }

    public NonNullList<FluidIngredient> getTopFluidIngredients() {
        return topFluidIngredients;
    }

    public NonNullList<FluidIngredient> getFluidCatalysts() {
        return fluidCatalysts;
    }

    public NonNullList<FluidIngredient> getBottomFluidIngredients() {
        return bottomFluidIngredients;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.addAll(topIngredients);
        ingredients.addAll(bottomIngredients);
        return ingredients;
    }

    public NonNullList<FluidIngredient> getFluidIngredients() {
        NonNullList<FluidIngredient> fluidIngredients = NonNullList.create();
        fluidIngredients.addAll(topFluidIngredients);
        fluidIngredients.addAll(bottomFluidIngredients);
        return fluidIngredients;
    }

    public List<ProcessingOutput> getRollableResults() {
        return results;
    }

    public NonNullList<FluidStack> getFluidResults() {
        return fluidResults;
    }

    public List<ItemStack> getRollableResultsAsItemStacks() {
        return getRollableResults().stream()
                .map(ProcessingOutput::getStack)
                .collect(Collectors.toList());
    }

    public void enforceNextResult(Supplier<ItemStack> stack) {
        forcedResult = stack;
    }

    public List<ItemStack> rollResults() {
        return rollResults(this.getRollableResults());
    }

    public List<ItemStack> rollResults(List<ProcessingOutput> rollableResults) {
        List<ItemStack> results = new ArrayList<>();
        for (int i = 0; i < rollableResults.size(); i++) {
            ProcessingOutput output = rollableResults.get(i);
            ItemStack stack = i == 0 && forcedResult != null ? forcedResult.get() : output.rollOutput();
            if (!stack.isEmpty())
                results.add(stack);
        }
        return results;
    }

    public double getChance() {
        return chances;
    }

//    public int getProcessingDuration() {
//        return processingDuration;
//    }

    public HeatCondition getRequiredHeat() {
        return requiredHeat;
    }

    public static boolean match(ChamberBlockEntity chamber, Recipe<?> recipe) {
        if (!(recipe instanceof ChamberRecipe chamberRecipe))
            return false;

        FilteringBehaviour filter = chamber.getFilter();
        if (filter == null)
            return false;

        boolean filterTest = filter.test(recipe.getResultItem(chamber.getLevel().registryAccess()));
        if (chamberRecipe.getRollableResults().isEmpty() && !chamberRecipe.getFluidResults().isEmpty())
            filterTest = filter.test(chamberRecipe.getFluidResults().get(0));

        if (!filterTest)
            return false;

        return apply(chamber, recipe, true);
    }

    public static boolean apply(ChamberBlockEntity chamber, Recipe<?> recipe) {
        return apply(chamber, recipe, false);
    }

    private static boolean apply(ChamberBlockEntity chamber, Recipe<?> recipe, boolean test) {
        if (!(recipe instanceof ChamberRecipe chamberRecipe))
            return false;

        ChamberTopBlockEntity topBE = chamber.getTop();
        ChamberMiddleBlockEntity middleBE = chamber.getMiddle();
        ChamberBottomBlockEntity bottomBE = chamber.getBottom();

        if (topBE == null || middleBE == null || bottomBE == null)
            return false;

        IItemHandler availableItemsTop = topBE.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        IFluidHandler availableFluidsTop = topBE.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        IItemHandler availableItemsMiddle = middleBE.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        IFluidHandler availableFluidsMiddle = middleBE.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        IItemHandler availableItemsBottom = bottomBE.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        IFluidHandler availableFluidsBottom = bottomBE.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);

        if (availableItemsTop == null || availableFluidsTop == null ||
                availableItemsMiddle == null || availableFluidsMiddle == null ||
                availableItemsBottom == null || availableFluidsBottom == null)
            return false;

        boolean catalystConsumed = r.nextDouble() < chamberRecipe.getChance();

        BlazeBurnerBlock.HeatLevel heat = getHeatLevelOf(chamber.getLevel()
                .getBlockState(bottomBE.getBlockPos().below(1)));
        if (!chamberRecipe.getRequiredHeat().testBlazeBurner(heat))
            return false;

        List<ItemStack> recipeOutputItems = new ArrayList<>();
        List<FluidStack> recipeOutputFluids = new ArrayList<>();

        for (boolean simulate : Iterate.trueAndFalse) {

            if (!simulate && test)
                return true;

            int[] extractedItemsTop = new int[availableItemsTop.getSlots()];
            int[] extractedFluidsTop = new int[availableFluidsTop.getTanks()];
            int[] extractedItemsMiddle = new int[availableItemsMiddle.getSlots()];
            int[] extractedFluidsMiddle = new int[availableFluidsMiddle.getTanks()];
            int[] extractedItemsBottom = new int[availableItemsBottom.getSlots()];
            int[] extractedFluidsBottom = new int[availableFluidsBottom.getTanks()];

            Ingredients:
            for (Ingredient ingredient : chamberRecipe.topIngredients) {
                for (int slot = 0; slot < availableItemsTop.getSlots(); slot++) {
                    if (simulate && availableItemsTop.getStackInSlot(slot)
                            .getCount() <= extractedItemsTop[slot])
                        continue;
                    ItemStack extracted = availableItemsTop.extractItem(slot, 1, true);
                    if (!ingredient.test(extracted))
                        continue;
                    if (!simulate)
                        availableItemsTop.extractItem(slot, 1, false);
                    extractedItemsTop[slot]++;
                    continue Ingredients;
                }

                // something wasn't found
                return false;
            }

            boolean fluidsAffected = false;
            FluidIngredients:
            for (FluidIngredient fluidIngredient : chamberRecipe.topFluidIngredients) {
                int amountRequired = fluidIngredient.getRequiredAmount();

                for (int tank = 0; tank < availableFluidsTop.getTanks(); tank++) {
                    FluidStack fluidStack = availableFluidsTop.getFluidInTank(tank);
                    if (simulate && fluidStack.getAmount() <= extractedFluidsTop[tank])
                        continue;
                    if (!fluidIngredient.test(fluidStack))
                        continue;
                    int drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
                    if (!simulate) {
                        fluidStack.shrink(drainedAmount);
                        fluidsAffected = true;
                    }
                    amountRequired -= drainedAmount;
                    if (amountRequired != 0)
                        continue;
                    extractedFluidsTop[tank] += drainedAmount;
                    continue FluidIngredients;
                }

                // something wasn't found
                return false;
            }

            if (fluidsAffected) {
                topBE.getBehaviour(SmartFluidTankBehaviour.INPUT)
                        .forEach(SmartFluidTankBehaviour.TankSegment::onFluidStackChanged);
            }

            Ingredients:
            for (Ingredient ingredient : chamberRecipe.catalysts) {
                for (int slot = 0; slot < availableItemsMiddle.getSlots(); slot++) {
                    if (simulate && availableItemsMiddle.getStackInSlot(slot)
                            .getCount() <= extractedItemsMiddle[slot])
                        continue;
                    ItemStack extracted = availableItemsMiddle.extractItem(slot, 1, true);
                    if (!ingredient.test(extracted))
                        continue;
                    if (!simulate && catalystConsumed)
                        availableItemsMiddle.extractItem(slot, 1, false);
                    extractedItemsMiddle[slot]++;
                    continue Ingredients;
                }

                // something wasn't found
                return false;
            }

            fluidsAffected = false;
            FluidIngredients:
            for (FluidIngredient fluidIngredient : chamberRecipe.fluidCatalysts) {
                int amountRequired = fluidIngredient.getRequiredAmount();

                for (int tank = 0; tank < availableFluidsMiddle.getTanks(); tank++) {
                    FluidStack fluidStack = availableFluidsMiddle.getFluidInTank(tank);
                    if (simulate && fluidStack.getAmount() <= extractedFluidsMiddle[tank])
                        continue;
                    if (!fluidIngredient.test(fluidStack))
                        continue;
                    int drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
                    if (!simulate && catalystConsumed) {
                        fluidStack.shrink(drainedAmount);
                        fluidsAffected = true;
                    }
                    amountRequired -= drainedAmount;
                    if (amountRequired != 0)
                        continue;
                    extractedFluidsMiddle[tank] += drainedAmount;
                    continue FluidIngredients;
                }

                // something wasn't found
                return false;
            }

            if (fluidsAffected) {
                middleBE.getBehaviour(SmartFluidTankBehaviour.INPUT)
                        .forEach(SmartFluidTankBehaviour.TankSegment::onFluidStackChanged);
            }

            Ingredients:
            for (Ingredient ingredient : chamberRecipe.bottomIngredients) {
                for (int slot = 0; slot < availableItemsBottom.getSlots(); slot++) {
                    if (simulate && availableItemsBottom.getStackInSlot(slot)
                            .getCount() <= extractedItemsBottom[slot])
                        continue;
                    ItemStack extracted = availableItemsBottom.extractItem(slot, 1, true);
                    if (!ingredient.test(extracted))
                        continue;
                    if (!simulate)
                        availableItemsBottom.extractItem(slot, 1, false);
                    extractedItemsBottom[slot]++;
                    continue Ingredients;
                }

                // something wasn't found
                return false;
            }

            fluidsAffected = false;
            FluidIngredients:
            for (FluidIngredient fluidIngredient : chamberRecipe.bottomFluidIngredients) {
                int amountRequired = fluidIngredient.getRequiredAmount();

                for (int tank = 0; tank < availableFluidsBottom.getTanks(); tank++) {
                    FluidStack fluidStack = availableFluidsBottom.getFluidInTank(tank);
                    if (simulate && fluidStack.getAmount() <= extractedFluidsBottom[tank])
                        continue;
                    if (!fluidIngredient.test(fluidStack))
                        continue;
                    int drainedAmount = Math.min(amountRequired, fluidStack.getAmount());
                    if (!simulate) {
                        fluidStack.shrink(drainedAmount);
                        fluidsAffected = true;
                    }
                    amountRequired -= drainedAmount;
                    if (amountRequired != 0)
                        continue;
                    extractedFluidsBottom[tank] += drainedAmount;
                    continue FluidIngredients;
                }

                // something wasn't found
                return false;
            }

            if (fluidsAffected) {
                bottomBE.getBehaviour(SmartFluidTankBehaviour.INPUT)
                        .forEach(SmartFluidTankBehaviour.TankSegment::onFluidStackChanged);
                bottomBE.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
                        .forEach(SmartFluidTankBehaviour.TankSegment::onFluidStackChanged);
            }

            if (simulate) {
                recipeOutputItems.addAll(chamberRecipe.rollResults());

                for (FluidStack fluidStack : chamberRecipe.getFluidResults())
                    if (!fluidStack.isEmpty())
                        recipeOutputFluids.add(fluidStack);
                for (ItemStack stack : chamberRecipe.getRemainingItems(topBE.getInputInventory(), extractedItemsTop))
                    if (!stack.isEmpty()) recipeOutputItems.add(stack);
                if (catalystConsumed)
                    for (ItemStack stack : chamberRecipe.getRemainingItems(middleBE.getInputInventory(), extractedItemsMiddle))
                        if (!stack.isEmpty()) recipeOutputItems.add(stack);
                for (ItemStack stack : chamberRecipe.getRemainingItems(bottomBE.getInputInventory(), extractedItemsBottom))
                    if (!stack.isEmpty()) recipeOutputItems.add(stack);
            }

            if (!bottomBE.acceptOutputs(recipeOutputItems, recipeOutputFluids, simulate))
                return false;
        }

        return true;
    }

    @Override
    public boolean matches(SmartInventory inv, @Nonnull Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(SmartInventory pContainer, RegistryAccess pRegistryAccess) {
        return getResultItem(pRegistryAccess);
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return getRollableResults().isEmpty() ? ItemStack.EMPTY : getRollableResults().get(0).getStack();
    }

    public NonNullList<ItemStack> getRemainingItems(SmartInventory container, int[] extracted) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack item = container.getItem(i);
            if (item.hasCraftingRemainingItem()) {
                nonnulllist.set(i, item.getCraftingRemainingItem().copyWithCount(extracted[i]));
            }
        }

        return nonnulllist;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String getGroup() {
        return "chamber";
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    public IRecipeTypeInfo getTypeInfo() {
        return typeInfo;
    }

}
