package net.dragonegg.sculkcatalyticchamber.content.chamber;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.data.SimpleDatagenIngredient;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.Pair;
import com.tterrag.registrate.util.DataIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChamberRecipeBuilder {
    
    protected ChamberRecipeParams params;
    protected List<ICondition> recipeConditions;

    public ChamberRecipeBuilder(ResourceLocation recipeId) {
        params = new ChamberRecipeParams(recipeId);
        recipeConditions = new ArrayList<>();
    }

    public ChamberRecipeBuilder withItemTopIngredients(Ingredient... ingredients) {
        return withItemTopIngredients(NonNullList.of(Ingredient.EMPTY, ingredients));
    }

    public ChamberRecipeBuilder withItemTopIngredients(NonNullList<Ingredient> ingredients) {
        params.topIngredients = ingredients;
        return this;
    }

    public ChamberRecipeBuilder withFluidTopIngredients(FluidIngredient... ingredients) {
        return withFluidTopIngredients(NonNullList.of(FluidIngredient.EMPTY, ingredients));
    }

    public ChamberRecipeBuilder withFluidTopIngredients(NonNullList<FluidIngredient> ingredients) {
        params.topFluidIngredients = ingredients;
        return this;
    }

    public ChamberRecipeBuilder withItemBottomIngredients(Ingredient... ingredients) {
        return withItemBottomIngredients(NonNullList.of(Ingredient.EMPTY, ingredients));
    }

    public ChamberRecipeBuilder withItemBottomIngredients(NonNullList<Ingredient> ingredients) {
        params.bottomIngredients = ingredients;
        return this;
    }

    public ChamberRecipeBuilder withFluidBottomIngredients(FluidIngredient... ingredients) {
        return withFluidBottomIngredients(NonNullList.of(FluidIngredient.EMPTY, ingredients));
    }

    public ChamberRecipeBuilder withFluidBottomIngredients(NonNullList<FluidIngredient> ingredients) {
        params.bottomFluidIngredients = ingredients;
        return this;
    }

    public ChamberRecipeBuilder withItemCatalysts(Ingredient... ingredients) {
        return withItemCatalysts(NonNullList.of(Ingredient.EMPTY, ingredients));
    }

    public ChamberRecipeBuilder withItemCatalysts(NonNullList<Ingredient> ingredients) {
        params.catalysts = ingredients;
        return this;
    }

    public ChamberRecipeBuilder withFluidCatalysts(FluidIngredient... ingredients) {
        return withFluidCatalysts(NonNullList.of(FluidIngredient.EMPTY, ingredients));
    }

    public ChamberRecipeBuilder withFluidCatalysts(NonNullList<FluidIngredient> ingredients) {
        params.fluidCatalysts = ingredients;
        return this;
    }

    public ChamberRecipeBuilder neverConsumed() {
        return withChances(0.0);
    }

    public ChamberRecipeBuilder alwaysConsumed() {
        return withChances(1.0);
    }

    public ChamberRecipeBuilder withChances(double chances) {
        params.chances = chances;
        return this;
    }

    public ChamberRecipeBuilder withSingleItemOutput(ItemStack output) {
        return withItemOutputs(new ProcessingOutput(output, 1));
    }

    public ChamberRecipeBuilder withItemOutputs(ProcessingOutput... outputs) {
        return withItemOutputs(NonNullList.of(ProcessingOutput.EMPTY, outputs));
    }

    public ChamberRecipeBuilder withItemOutputs(NonNullList<ProcessingOutput> outputs) {
        params.results = outputs;
        return this;
    }

    public ChamberRecipeBuilder withFluidOutputs(FluidStack... outputs) {
        return withFluidOutputs(NonNullList.of(FluidStack.EMPTY, outputs));
    }

    public ChamberRecipeBuilder withFluidOutputs(NonNullList<FluidStack> outputs) {
        params.fluidResults = outputs;
        return this;
    }

//    public ChamberRecipeBuilder duration(int ticks) {
//        params.processingDuration = ticks;
//        return this;
//    }
//
//    public ChamberRecipeBuilder averageProcessingDuration() {
//        return duration(100);
//    }

    public ChamberRecipeBuilder requiresHeat(HeatCondition condition) {
        params.requiredHeat = condition;
        return this;
    }

    public ChamberRecipe build() {
        return new ChamberRecipe(params);
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new DataGenResult(build(), recipeConditions));
    }

    // Datagen shortcuts

    public ChamberRecipeBuilder topRequire(TagKey<Item> tag) {
        return topRequire(Ingredient.of(tag));
    }

    public ChamberRecipeBuilder topRequire(ItemLike item) {
        return topRequire(Ingredient.of(item));
    }

    public ChamberRecipeBuilder topRequire(Ingredient ingredient) {
        params.topIngredients.add(ingredient);
        return this;
    }

    public ChamberRecipeBuilder topRequire(Mods mod, String id) {
        params.topIngredients.add(new SimpleDatagenIngredient(mod, id));
        return this;
    }

    public ChamberRecipeBuilder topRequire(ResourceLocation ingredient) {
        params.topIngredients.add(DataIngredient.ingredient(null, ingredient));
        return this;
    }

    public ChamberRecipeBuilder topRequire(Fluid fluid, int amount) {
        return topRequire(FluidIngredient.fromFluid(fluid, amount));
    }

    public ChamberRecipeBuilder topRequire(TagKey<Fluid> fluidTag, int amount) {
        return topRequire(FluidIngredient.fromTag(fluidTag, amount));
    }

    public ChamberRecipeBuilder topRequire(FluidIngredient ingredient) {
        params.topFluidIngredients.add(ingredient);
        return this;
    }

    public ChamberRecipeBuilder bottomRequire(TagKey<Item> tag) {
        return bottomRequire(Ingredient.of(tag));
    }

    public ChamberRecipeBuilder bottomRequire(ItemLike item) {
        return bottomRequire(Ingredient.of(item));
    }

    public ChamberRecipeBuilder bottomRequire(Ingredient ingredient) {
        params.bottomIngredients.add(ingredient);
        return this;
    }

    public ChamberRecipeBuilder bottomRequire(Mods mod, String id) {
        params.bottomIngredients.add(new SimpleDatagenIngredient(mod, id));
        return this;
    }

    public ChamberRecipeBuilder bottomRequire(ResourceLocation ingredient) {
        params.bottomIngredients.add(DataIngredient.ingredient(null, ingredient));
        return this;
    }

    public ChamberRecipeBuilder bottomRequire(Fluid fluid, int amount) {
        return bottomRequire(FluidIngredient.fromFluid(fluid, amount));
    }

    public ChamberRecipeBuilder bottomRequire(TagKey<Fluid> fluidTag, int amount) {
        return bottomRequire(FluidIngredient.fromTag(fluidTag, amount));
    }

    public ChamberRecipeBuilder bottomRequire(FluidIngredient ingredient) {
        params.bottomFluidIngredients.add(ingredient);
        return this;
    }

    public ChamberRecipeBuilder catalysts(TagKey<Item> tag) {
        return catalysts(Ingredient.of(tag));
    }

    public ChamberRecipeBuilder catalysts(ItemLike item) {
        return catalysts(Ingredient.of(item));
    }

    public ChamberRecipeBuilder catalysts(Ingredient ingredient) {
        params.catalysts.add(ingredient);
        return this;
    }

    public ChamberRecipeBuilder catalysts(Mods mod, String id) {
        params.catalysts.add(new SimpleDatagenIngredient(mod, id));
        return this;
    }

    public ChamberRecipeBuilder catalysts(ResourceLocation ingredient) {
        params.catalysts.add(DataIngredient.ingredient(null, ingredient));
        return this;
    }

    public ChamberRecipeBuilder catalysts(Fluid fluid, int amount) {
        return catalysts(FluidIngredient.fromFluid(fluid, amount));
    }

    public ChamberRecipeBuilder catalysts(TagKey<Fluid> fluidTag, int amount) {
        return catalysts(FluidIngredient.fromTag(fluidTag, amount));
    }

    public ChamberRecipeBuilder catalysts(FluidIngredient ingredient) {
        params.fluidCatalysts.add(ingredient);
        return this;
    }

    public ChamberRecipeBuilder output(ItemLike item) {
        return output(item, 1);
    }

    public ChamberRecipeBuilder output(float chance, ItemLike item) {
        return output(chance, item, 1);
    }

    public ChamberRecipeBuilder output(ItemLike item, int amount) {
        return output(1, item, amount);
    }

    public ChamberRecipeBuilder output(float chance, ItemLike item, int amount) {
        return output(chance, new ItemStack(item, amount));
    }

    public ChamberRecipeBuilder output(ItemStack output) {
        return output(1, output);
    }

    public ChamberRecipeBuilder output(float chance, ItemStack output) {
        return output(new ProcessingOutput(output, chance));
    }

    public ChamberRecipeBuilder output(float chance, Mods mod, String id, int amount) {
        return output(new ProcessingOutput(Pair.of(mod.asResource(id), amount), chance));
    }

    public ChamberRecipeBuilder output(Mods mod, String id) {
        return output(1, mod.asResource(id), 1);
    }

    public ChamberRecipeBuilder output(float chance, ResourceLocation registryName, int amount) {
        return output(new ProcessingOutput(Pair.of(registryName, amount), chance));
    }

    public ChamberRecipeBuilder output(ProcessingOutput output) {
        params.results.add(output);
        return this;
    }

    public ChamberRecipeBuilder output(Fluid fluid, int amount) {
        fluid = FluidHelper.convertToStill(fluid);
        return output(new FluidStack(fluid, amount));
    }

    public ChamberRecipeBuilder output(FluidStack fluidStack) {
        params.fluidResults.add(fluidStack);
        return this;
    }

    public ChamberRecipeBuilder whenModLoaded(String modid) {
        return withCondition(new ModLoadedCondition(modid));
    }

    public ChamberRecipeBuilder whenModMissing(String modid) {
        return withCondition(new NotCondition(new ModLoadedCondition(modid)));
    }

    public ChamberRecipeBuilder withCondition(ICondition condition) {
        recipeConditions.add(condition);
        return this;
    }

    public static class ChamberRecipeParams {

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
//        protected int processingDuration;
        protected HeatCondition requiredHeat;

        protected ChamberRecipeParams(ResourceLocation id) {
            this.id = id;
            topIngredients = NonNullList.create();
            topFluidIngredients = NonNullList.create();
            bottomIngredients = NonNullList.create();
            bottomFluidIngredients = NonNullList.create();
            catalysts = NonNullList.create();
            fluidCatalysts = NonNullList.create();
            chances = 0.0;
            results = NonNullList.create();
            fluidResults = NonNullList.create();
//            processingDuration = 0;
            requiredHeat = HeatCondition.NONE;
        }

    }

    public static class DataGenResult implements FinishedRecipe {

        private List<ICondition> recipeConditions;
        private ChamberRecipeSerializer serializer;
        private ResourceLocation id;
        private ChamberRecipe recipe;

        public DataGenResult(ChamberRecipe recipe, List<ICondition> recipeConditions) {
            this.recipe = recipe;
            this.recipeConditions = recipeConditions;
            IRecipeTypeInfo recipeType = this.recipe.getTypeInfo();

            if (!(recipeType.getSerializer() instanceof ChamberRecipeSerializer))
                throw new IllegalStateException("Cannot datagen ChamberRecipe");

            this.id = recipe.getId();
            this.serializer = (ChamberRecipeSerializer) recipe.getSerializer();
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            serializer.write(json, recipe);
            if (recipeConditions.isEmpty())
                return;

            JsonArray conds = new JsonArray();
            recipeConditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
            json.add("conditions", conds);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return serializer;
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }

    }
    
}
