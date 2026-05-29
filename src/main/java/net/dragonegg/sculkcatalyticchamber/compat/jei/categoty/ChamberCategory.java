package net.dragonegg.sculkcatalyticchamber.compat.jei.categoty;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.data.Pair;
import net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber;
import net.dragonegg.sculkcatalyticchamber.compat.jei.categoty.animations.AnimatedMechanicalShrieker;
import net.dragonegg.sculkcatalyticchamber.content.chamber.ChamberRecipe;
import net.dragonegg.sculkcatalyticchamber.content.chamber.FluidIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChamberCategory extends CreateRecipeCategory<ChamberRecipe> {

    private final AnimatedMechanicalShrieker shrieker = new AnimatedMechanicalShrieker();
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

    public ChamberCategory(Info<ChamberRecipe> info) {
        super(info);
    }

    @Override
    protected void setRecipe(IRecipeLayoutBuilder builder, ChamberRecipe recipe, IFocusGroup focuses) {
        int bottomRows = setIngredients(builder, recipe.getBottomIngredients(), recipe.getBottomFluidIngredients(), 0);
        setIngredients(builder, recipe.getTopIngredients(), recipe.getTopFluidIngredients(),
                -40 + (bottomRows > 1 ? -19 : 0));

        int resultRows = setResults(builder, recipe.getRollableResults(), recipe.getFluidResults());
        setCatalysts(builder, recipe.getCatalysts(), recipe.getFluidCatalysts(),
                resultRows > 1 ? -23 : 0, (float) recipe.getChance());

        HeatCondition requiredHeat = recipe.getRequiredHeat();
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 141)
                    .addItemStack(AllBlocks.BLAZE_BURNER.asStack());
        }
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED)) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 153, 141)
                    .addItemStack(AllItems.BLAZE_CAKE.asStack());
        }
    }

    private int setIngredients(IRecipeLayoutBuilder builder, NonNullList<Ingredient> ingredients,
                               NonNullList<FluidIngredient> fluidIngredients, int yOffset) {
        List<Pair<Ingredient, MutableInt>> condensedIngredients = ItemHelper.condenseIngredients(ingredients);

        int size = condensedIngredients.size() + fluidIngredients.size();
        int index = 0;

        for (Pair<Ingredient, MutableInt> pair : condensedIngredients) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack itemStack : pair.getFirst().getItems()) {
                ItemStack copy = itemStack.copy();
                copy.setCount(pair.getSecond().getValue());
                stacks.add(copy);
            }

            int x = 8 + (index % 4 + (size < 4 ? 4 - size : 0)) * 19;
            int y = 101 + yOffset - (index / 4) * 19;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addItemStacks(stacks);
            index++;
        }

        for (FluidIngredient fluidIngredient : fluidIngredients) {
            int x = 8 + (index % 4 + (size < 4 ? 4 - size : 0)) * 19;
            int y = 101 + yOffset - (index / 4) * 19;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .setFluidRenderer(Math.max(1000, fluidIngredient.getRequiredAmount()), false, 16, 16)
                    .addIngredients(NeoForgeTypes.FLUID_STACK, withImprovedVisibility(fluidIngredient.getMatchingFluidStacks()))
                    .addTooltipCallback(addFluidTooltip(fluidIngredient.getRequiredAmount()));
            index++;
        }
        return (index - 1) / 4 + 1;
    }

    private void setCatalysts(IRecipeLayoutBuilder builder, NonNullList<Ingredient> ingredients,
                              NonNullList<FluidIngredient> fluidIngredients, int yOffset, float chance) {
        List<Pair<Ingredient, MutableInt>> condensedIngredients = ItemHelper.condenseIngredients(ingredients);

        int index = 0;

        for (Pair<Ingredient, MutableInt> pair : condensedIngredients) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack itemStack : pair.getFirst().getItems()) {
                ItemStack copy = itemStack.copy();
                copy.setCount(pair.getSecond().getValue());
                stacks.add(copy);
            }

            int x = 131 + (index % 3) * 19;
            int y = 83 + yOffset - (index / 3) * 19;
            builder.addSlot(RecipeIngredientRole.CATALYST, x, y)
                    .setBackground(getRenderedSlot(chance), -1, -1)
                    .addItemStacks(stacks)
                    .addTooltipCallback(addStochasticTooltip(chance));
            index++;
        }

        for (FluidIngredient fluidIngredient : fluidIngredients) {
            int x = 131 + (index % 3) * 19;
            int y = 83 + yOffset - (index / 3) * 19;
            builder.addSlot(RecipeIngredientRole.CATALYST, x, y)
                    .setBackground(getRenderedSlot(chance), -1, -1)
                    .setFluidRenderer(Math.max(1000, fluidIngredient.getRequiredAmount()), false, 16, 16)
                    .addIngredients(NeoForgeTypes.FLUID_STACK, withImprovedVisibility(fluidIngredient.getMatchingFluidStacks()))
                    .addTooltipCallback(addFluidTooltip(fluidIngredient.getRequiredAmount()))
                    .addTooltipCallback(addStochasticTooltip(chance));
            index++;
        }
    }

    private int setResults(IRecipeLayoutBuilder builder, List<ProcessingOutput> rollableResults,
                           NonNullList<FluidStack> fluidResults) {
        int size = rollableResults.size() + fluidResults.size();
        int xOffset = size < 3 ? (3 - size) * 19 / 2 : 0;
        int index = 0;

        for (ProcessingOutput result : rollableResults) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 131 + xOffset + (index % 3) * 19, 121 - (index / 3) * 19)
                    .setBackground(getRenderedSlot(result), -1, -1)
                    .addItemStack(result.getStack())
                    .addRichTooltipCallback(CreateRecipeCategory.addStochasticTooltip(result));
            index++;
        }

        for (FluidStack fluidResult : fluidResults) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 131 + xOffset + (index % 3) * 19, 121 - (index / 3) * 19)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .setFluidRenderer(Math.max(1000, fluidResult.getAmount()), false, 16, 16)
                    .addIngredient(NeoForgeTypes.FLUID_STACK, withImprovedVisibility(fluidResult))
                    .addTooltipCallback(addFluidTooltip(fluidResult.getAmount()));
            index++;
        }
        return (index - 1) / 3 + 1;
    }

    @Override
    protected void draw(ChamberRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                        double mouseX, double mouseY) {
        ResourceLocation location = SculkCatalyticChamber.asResource("textures/gui/jei/widgets.png");
        HeatCondition requiredHeat = recipe.getRequiredHeat();

        int resultRows = (recipe.getFluidResults().size() + recipe.getRollableResults().size() - 1) / 3 + 1;
        if (resultRows <= 1) {
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 144, 104);
        } else {
            graphics.blit(location, 125, 80, 19, 21, 17, 15);
        }

        boolean noHeat = requiredHeat == HeatCondition.NONE;
        AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
        shadow.render(graphics, 81, 108 + (noHeat ? 10 : 30));

        graphics.blit(location, 12, 140, 0, noHeat ? 221 : 201, 169, 19);

        graphics.drawString(Minecraft.getInstance().font, CreateLang.translateDirect(requiredHeat.getTranslationKey()),
                17, 146, requiredHeat.getColor(), false);

        if (requiredHeat != HeatCondition.NONE) {
            heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
                    .draw(graphics, getBackground().getWidth() / 2 - 5, 105);
        }
        shrieker.draw(graphics, getBackground().getWidth() / 2 - 5, 32);
    }

    private static List<FluidStack> withImprovedVisibility(FluidStack[] stacks) {
        return Arrays.stream(stacks)
                .map(ChamberCategory::withImprovedVisibility)
                .toList();
    }

    private static FluidStack withImprovedVisibility(FluidStack stack) {
        return stack.copyWithAmount(Math.max(1000, stack.getAmount()));
    }

    public static IRecipeSlotTooltipCallback addFluidTooltip(int amount) {
        return (view, tooltip) -> tooltip.add(Component.literal(amount + " mB").withStyle(ChatFormatting.GRAY));
    }

    public static IRecipeSlotTooltipCallback addStochasticTooltip(float chance) {
        return (view, tooltip) -> {
            if (chance == 1) {
                return;
            }
            MutableComponent component = chance == 0
                    ? CreateLang.translateDirect("recipe.deploying.not_consumed")
                    : CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100));
            tooltip.add(1, component.withStyle(ChatFormatting.GOLD));
        };
    }
}
