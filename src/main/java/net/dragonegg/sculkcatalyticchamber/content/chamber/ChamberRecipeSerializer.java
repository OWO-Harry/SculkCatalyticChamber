package net.dragonegg.sculkcatalyticchamber.content.chamber;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class ChamberRecipeSerializer implements RecipeSerializer<ChamberRecipe> {

    private static final Codec<Either<FluidIngredient, Ingredient>> MIXED_INGREDIENT_CODEC =
            Codec.either(FluidIngredient.CODEC, Ingredient.CODEC);
    private static final Codec<FluidStack> LEGACY_FLUID_STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidStack::getFluid),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(FluidStack::getAmount)
    ).apply(instance, FluidStack::new));
    private static final Codec<ProcessingOutput> LEGACY_PROCESSING_OUTPUT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(output -> output.getStack().getItem()),
            ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(output -> output.getStack().getCount()),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("chance", 1F).forGetter(ProcessingOutput::getChance)
    ).apply(instance, (Item item, Integer count, Float chance) -> new ProcessingOutput(item, count, chance)));
    private static final Codec<Either<FluidStack, ProcessingOutput>> MIXED_RESULT_CODEC =
            Codec.either(Codec.withAlternative(FluidStack.CODEC, LEGACY_FLUID_STACK_CODEC),
                    Codec.withAlternative(ProcessingOutput.CODEC, LEGACY_PROCESSING_OUTPUT_CODEC));

    private static final MapCodec<ChamberRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MIXED_INGREDIENT_CODEC.listOf().optionalFieldOf("topIngredients", List.of()).forGetter(recipe -> mixedIngredients(recipe.topIngredients, recipe.topFluidIngredients)),
            FluidIngredient.CODEC.listOf().optionalFieldOf("topFluidIngredients", List.of()).forGetter(recipe -> List.of()),
            MIXED_INGREDIENT_CODEC.listOf().optionalFieldOf("bottomIngredients", List.of()).forGetter(recipe -> mixedIngredients(recipe.bottomIngredients, recipe.bottomFluidIngredients)),
            FluidIngredient.CODEC.listOf().optionalFieldOf("bottomFluidIngredients", List.of()).forGetter(recipe -> List.of()),
            MIXED_INGREDIENT_CODEC.listOf().optionalFieldOf("catalysts", List.of()).forGetter(recipe -> mixedIngredients(recipe.catalysts, recipe.fluidCatalysts)),
            FluidIngredient.CODEC.listOf().optionalFieldOf("fluidCatalysts", List.of()).forGetter(recipe -> List.of()),
            Codec.DOUBLE.optionalFieldOf("chances", 0.0).forGetter(ChamberRecipe::getChance),
            MIXED_RESULT_CODEC.listOf().optionalFieldOf("results", List.of()).forGetter(recipe -> mixedResults(recipe.results, recipe.fluidResults)),
            Codec.withAlternative(FluidStack.CODEC, LEGACY_FLUID_STACK_CODEC).listOf().optionalFieldOf("fluidResults", List.of()).forGetter(recipe -> List.of()),
            HeatCondition.CODEC.optionalFieldOf("heatRequirement", HeatCondition.NONE).forGetter(ChamberRecipe::getRequiredHeat)
    ).apply(instance, ChamberRecipeSerializer::fromCodec));

    private static final StreamCodec<RegistryFriendlyByteBuf, ChamberRecipe> STREAM_CODEC = StreamCodec.of(
            ChamberRecipeSerializer::toNetwork,
            ChamberRecipeSerializer::fromNetwork
    );

    private static ChamberRecipe fromCodec(List<Either<FluidIngredient, Ingredient>> topIngredients,
                                           List<FluidIngredient> topFluidIngredients,
                                           List<Either<FluidIngredient, Ingredient>> bottomIngredients,
                                           List<FluidIngredient> bottomFluidIngredients,
                                           List<Either<FluidIngredient, Ingredient>> catalysts,
                                           List<FluidIngredient> fluidCatalysts,
                                           double chances,
                                           List<Either<FluidStack, ProcessingOutput>> results,
                                           List<FluidStack> fluidResults,
                                           HeatCondition requiredHeat) {
        ChamberRecipeBuilder.ChamberRecipeParams params = new ChamberRecipeBuilder.ChamberRecipeParams(null);
        splitIngredients(topIngredients, params.topIngredients, params.topFluidIngredients);
        params.topFluidIngredients.addAll(topFluidIngredients);
        splitIngredients(bottomIngredients, params.bottomIngredients, params.bottomFluidIngredients);
        params.bottomFluidIngredients.addAll(bottomFluidIngredients);
        splitIngredients(catalysts, params.catalysts, params.fluidCatalysts);
        params.fluidCatalysts.addAll(fluidCatalysts);
        params.chances = chances;
        splitResults(results, params.results, params.fluidResults);
        params.fluidResults.addAll(fluidResults);
        params.requiredHeat = requiredHeat;
        return new ChamberRecipe(params);
    }

    private static ChamberRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        ChamberRecipeBuilder.ChamberRecipeParams params = new ChamberRecipeBuilder.ChamberRecipeParams(null);
        params.topIngredients = toNonNullList(readList(buffer, Ingredient.CONTENTS_STREAM_CODEC));
        params.topFluidIngredients = toNonNullList(readFluidIngredientList(buffer));
        params.bottomIngredients = toNonNullList(readList(buffer, Ingredient.CONTENTS_STREAM_CODEC));
        params.bottomFluidIngredients = toNonNullList(readFluidIngredientList(buffer));
        params.catalysts = toNonNullList(readList(buffer, Ingredient.CONTENTS_STREAM_CODEC));
        params.fluidCatalysts = toNonNullList(readFluidIngredientList(buffer));
        params.chances = buffer.readDouble();
        params.results = toNonNullList(readList(buffer, ProcessingOutput.STREAM_CODEC));
        params.fluidResults = toNonNullList(readList(buffer, FluidStack.STREAM_CODEC));
        params.requiredHeat = HeatCondition.STREAM_CODEC.decode(buffer);
        return new ChamberRecipe(params);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, ChamberRecipe recipe) {
        writeList(buffer, recipe.topIngredients, Ingredient.CONTENTS_STREAM_CODEC);
        writeFluidIngredientList(buffer, recipe.topFluidIngredients);
        writeList(buffer, recipe.bottomIngredients, Ingredient.CONTENTS_STREAM_CODEC);
        writeFluidIngredientList(buffer, recipe.bottomFluidIngredients);
        writeList(buffer, recipe.catalysts, Ingredient.CONTENTS_STREAM_CODEC);
        writeFluidIngredientList(buffer, recipe.fluidCatalysts);
        buffer.writeDouble(recipe.chances);
        writeList(buffer, recipe.results, ProcessingOutput.STREAM_CODEC);
        writeList(buffer, recipe.fluidResults, FluidStack.STREAM_CODEC);
        HeatCondition.STREAM_CODEC.encode(buffer, recipe.requiredHeat);
    }

    private static <T> NonNullList<T> toNonNullList(List<T> list) {
        NonNullList<T> result = NonNullList.create();
        result.addAll(list);
        return result;
    }

    private static List<Either<FluidIngredient, Ingredient>> mixedIngredients(List<Ingredient> itemIngredients,
                                                                              List<FluidIngredient> fluidIngredients) {
        List<Either<FluidIngredient, Ingredient>> mixed = new ArrayList<>();
        itemIngredients.forEach(ingredient -> mixed.add(Either.right(ingredient)));
        fluidIngredients.forEach(ingredient -> mixed.add(Either.left(ingredient)));
        return mixed;
    }

    private static void splitIngredients(List<Either<FluidIngredient, Ingredient>> mixed,
                                         NonNullList<Ingredient> itemIngredients,
                                         NonNullList<FluidIngredient> fluidIngredients) {
        mixed.forEach(ingredient -> ingredient.ifLeft(fluidIngredients::add).ifRight(itemIngredients::add));
    }

    private static List<Either<FluidStack, ProcessingOutput>> mixedResults(List<ProcessingOutput> itemResults,
                                                                           List<FluidStack> fluidResults) {
        List<Either<FluidStack, ProcessingOutput>> mixed = new ArrayList<>();
        itemResults.forEach(result -> mixed.add(Either.right(result)));
        fluidResults.forEach(result -> mixed.add(Either.left(result)));
        return mixed;
    }

    private static void splitResults(List<Either<FluidStack, ProcessingOutput>> mixed,
                                     NonNullList<ProcessingOutput> itemResults,
                                     NonNullList<FluidStack> fluidResults) {
        mixed.forEach(result -> result.ifLeft(fluidResults::add).ifRight(itemResults::add));
    }

    private static <T> List<T> readList(RegistryFriendlyByteBuf buffer, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        return buffer.readList(buf -> codec.decode((RegistryFriendlyByteBuf) buf));
    }

    private static <T> void writeList(RegistryFriendlyByteBuf buffer, List<T> list, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        buffer.writeCollection(list, (buf, value) -> codec.encode((RegistryFriendlyByteBuf) buf, value));
    }

    private static List<FluidIngredient> readFluidIngredientList(RegistryFriendlyByteBuf buffer) {
        return buffer.readList(FluidIngredient::read);
    }

    private static void writeFluidIngredientList(RegistryFriendlyByteBuf buffer, List<FluidIngredient> list) {
        buffer.writeCollection(list, (buf, value) -> value.write(buf));
    }

    @Override
    public MapCodec<ChamberRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChamberRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
