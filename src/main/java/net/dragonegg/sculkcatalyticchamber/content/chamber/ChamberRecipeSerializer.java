package net.dragonegg.sculkcatalyticchamber.content.chamber;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

public class ChamberRecipeSerializer implements RecipeSerializer<ChamberRecipe> {

    public ChamberRecipeSerializer() {
    }

    public final void write(JsonObject json, ChamberRecipe recipe) {
        JsonArray jsonTopIngredients = new JsonArray();
        JsonArray jsonBottomIngredients = new JsonArray();
        JsonArray jsonCatalysts = new JsonArray();
        JsonArray jsonOutputs = new JsonArray();

        recipe.topIngredients.forEach(i -> jsonTopIngredients.add(i.toJson()));
        recipe.topFluidIngredients.forEach(i -> jsonTopIngredients.add(i.serialize()));

        recipe.bottomIngredients.forEach(i -> jsonBottomIngredients.add(i.toJson()));
        recipe.bottomFluidIngredients.forEach(i -> jsonBottomIngredients.add(i.serialize()));

        recipe.catalysts.forEach(i -> jsonCatalysts.add(i.toJson()));
        recipe.fluidCatalysts.forEach(i -> jsonCatalysts.add(i.serialize()));

        recipe.results.forEach(o -> jsonOutputs.add(o.serialize()));
        recipe.fluidResults.forEach(o -> jsonOutputs.add(FluidHelper.serializeFluidStack(o)));

        json.add("topIngredients", jsonTopIngredients);
        json.add("bottomIngredients", jsonBottomIngredients);
        json.add("catalysts", jsonCatalysts);
        json.add("results", jsonOutputs);

        double chances = recipe.getChance();
        if (chances > 0)
            json.addProperty("chances", chances);

//        int processingDuration = recipe.getProcessingDuration();
//        if (processingDuration > 0)
//            json.addProperty("processingTime", processingDuration);

        HeatCondition requiredHeat = recipe.getRequiredHeat();
        if (requiredHeat != HeatCondition.NONE)
            json.addProperty("heatRequirement", requiredHeat.serialize());

    }

    @Override
    public final ChamberRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        ChamberRecipeBuilder builder = new ChamberRecipeBuilder(recipeId);
        NonNullList<Ingredient> topIngredients = NonNullList.create();
        NonNullList<FluidIngredient> topFluidIngredients = NonNullList.create();
        NonNullList<Ingredient> bottomIngredients = NonNullList.create();
        NonNullList<FluidIngredient> bottomFluidIngredients = NonNullList.create();
        NonNullList<Ingredient> catalysts = NonNullList.create();
        NonNullList<FluidIngredient> fluidCatalysts = NonNullList.create();
        NonNullList<ProcessingOutput> results = NonNullList.create();
        NonNullList<FluidStack> fluidResults = NonNullList.create();

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "topIngredients")) {
            if (FluidIngredient.isFluidIngredient(je))
                topFluidIngredients.add(FluidIngredient.deserialize(je));
            else
                topIngredients.add(Ingredient.fromJson(je));
        }

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "bottomIngredients")) {
            if (FluidIngredient.isFluidIngredient(je))
                bottomFluidIngredients.add(FluidIngredient.deserialize(je));
            else
                bottomIngredients.add(Ingredient.fromJson(je));
        }

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "catalysts")) {
            if (FluidIngredient.isFluidIngredient(je))
                fluidCatalysts.add(FluidIngredient.deserialize(je));
            else
                catalysts.add(Ingredient.fromJson(je));
        }

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "results")) {
            JsonObject jsonObject = je.getAsJsonObject();
            if (GsonHelper.isValidNode(jsonObject, "fluid"))
                fluidResults.add(FluidHelper.deserializeFluidStack(jsonObject));
            else
                results.add(ProcessingOutput.deserialize(je));
        }

        builder.withItemTopIngredients(topIngredients)
                .withFluidTopIngredients(topFluidIngredients)
                .withItemBottomIngredients(bottomIngredients)
                .withFluidBottomIngredients(bottomFluidIngredients)
                .withItemCatalysts(catalysts)
                .withFluidCatalysts(fluidCatalysts)
                .withItemOutputs(results)
                .withFluidOutputs(fluidResults);

        if (GsonHelper.isValidNode(json, "chances"))
            builder.withChances(GsonHelper.getAsDouble(json, "chances"));
//        if (GsonHelper.isValidNode(json, "processingTime"))
//            builder.duration(GsonHelper.getAsInt(json, "processingTime"));
        if (GsonHelper.isValidNode(json, "heatRequirement"))
            builder.requiresHeat(HeatCondition.deserialize(GsonHelper.getAsString(json, "heatRequirement")));

        ChamberRecipe recipe = builder.build();
        return recipe;
    }

    @Override
    public final void toNetwork(FriendlyByteBuf buffer, ChamberRecipe recipe) {
        NonNullList<Ingredient> topIngredients = recipe.topIngredients;
        NonNullList<FluidIngredient> topFluidIngredients = recipe.topFluidIngredients;
        NonNullList<Ingredient> botttomIngredients = recipe.bottomIngredients;
        NonNullList<FluidIngredient> bottomFluidIngredients = recipe.bottomFluidIngredients;
        NonNullList<Ingredient> catalysts = recipe.catalysts;
        NonNullList<FluidIngredient> fluidCatalysts = recipe.fluidCatalysts;
        NonNullList<ProcessingOutput> outputs = recipe.results;
        NonNullList<FluidStack> fluidOutputs = recipe.fluidResults;

        buffer.writeVarInt(topIngredients.size());
        topIngredients.forEach(i -> i.toNetwork(buffer));
        buffer.writeVarInt(topFluidIngredients.size());
        topFluidIngredients.forEach(i -> i.write(buffer));

        buffer.writeVarInt(botttomIngredients.size());
        botttomIngredients.forEach(i -> i.toNetwork(buffer));
        buffer.writeVarInt(bottomFluidIngredients.size());
        bottomFluidIngredients.forEach(i -> i.write(buffer));

        buffer.writeVarInt(catalysts.size());
        catalysts.forEach(i -> i.toNetwork(buffer));
        buffer.writeVarInt(fluidCatalysts.size());
        fluidCatalysts.forEach(i -> i.write(buffer));

        buffer.writeVarInt(outputs.size());
        outputs.forEach(o -> o.write(buffer));
        buffer.writeVarInt(fluidOutputs.size());
        fluidOutputs.forEach(o -> o.writeToPacket(buffer));

        buffer.writeDouble(recipe.getChance());
//        buffer.writeVarInt(recipe.getProcessingDuration());
        buffer.writeVarInt(recipe.getRequiredHeat().ordinal());
    }

    @Override
    public final ChamberRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        NonNullList<Ingredient> topIngredients = NonNullList.create();
        NonNullList<FluidIngredient> topFluidIngredients = NonNullList.create();
        NonNullList<Ingredient> bottomIngredients = NonNullList.create();
        NonNullList<FluidIngredient> bottomFluidIngredients = NonNullList.create();
        NonNullList<Ingredient> catalysts = NonNullList.create();
        NonNullList<FluidIngredient> fluidCatalysts = NonNullList.create();
        NonNullList<ProcessingOutput> results = NonNullList.create();
        NonNullList<FluidStack> fluidResults = NonNullList.create();

        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            topIngredients.add(Ingredient.fromNetwork(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            topFluidIngredients.add(FluidIngredient.read(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            bottomIngredients.add(Ingredient.fromNetwork(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            bottomFluidIngredients.add(FluidIngredient.read(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            catalysts.add(Ingredient.fromNetwork(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            fluidCatalysts.add(FluidIngredient.read(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            results.add(ProcessingOutput.read(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            fluidResults.add(FluidStack.readFromPacket(buffer));

        ChamberRecipe recipe = new ChamberRecipeBuilder(recipeId)
                .withItemTopIngredients(topIngredients)
                .withFluidTopIngredients(topFluidIngredients)
                .withItemBottomIngredients(bottomIngredients)
                .withFluidBottomIngredients(bottomFluidIngredients)
                .withItemCatalysts(catalysts)
                .withFluidCatalysts(fluidCatalysts)
                .withItemOutputs(results)
                .withFluidOutputs(fluidResults)
                .withChances(buffer.readDouble())
//                .duration(buffer.readVarInt())
                .requiresHeat(HeatCondition.values()[buffer.readVarInt()])
                .build();

        return recipe;
    }

}