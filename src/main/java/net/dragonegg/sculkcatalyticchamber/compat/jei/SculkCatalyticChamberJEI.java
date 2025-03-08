package net.dragonegg.sculkcatalyticchamber.compat.jei;

import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory.Info;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber;
import net.dragonegg.sculkcatalyticchamber.compat.jei.categoty.ChamberCategory;
import net.dragonegg.sculkcatalyticchamber.content.chamber.ChamberRecipe;
import net.dragonegg.sculkcatalyticchamber.registry.BlockRegistry;
import net.dragonegg.sculkcatalyticchamber.registry.RecipeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber.MODID;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class SculkCatalyticChamberJEI implements IModPlugin {

    private static final ResourceLocation ID = SculkCatalyticChamber.asResource("jei_plugin");

    private CreateRecipeCategory<ChamberRecipe> chamber;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    private void loadCategory() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        Supplier<ItemStack> supplier1 = () -> new ItemStack(BlockRegistry.CHAMBER_BOTTOM_BLOCK.get());
        Supplier<ItemStack> supplier2 = () -> new ItemStack(BlockRegistry.MECHANICAL_SHRIEKER_BLOCK.get());

        RecipeType<ChamberRecipe> recipeType = new RecipeType<>(
                SculkCatalyticChamber.asResource("chamber"), ChamberRecipe.class);
        Component title = Components.translatable(MODID + ".recipe.chamber");
        IDrawable background = new EmptyBackground(193, 163);
        IDrawable icon = new DoubleItemIcon(supplier1, supplier2);
        List<ChamberRecipe> recipes = connection == null? Collections.emptyList() :
                connection.getRecipeManager().getAllRecipesFor(RecipeRegistry.CHAMBER.getType());
        List<Supplier<? extends ItemStack>> catalysts = List.of(supplier1, supplier2);

        chamber = new ChamberCategory(new Info<>(recipeType, title, background, icon, () -> recipes, catalysts));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategory();
        registration.addRecipeCategories(chamber);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        chamber.registerRecipes(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        chamber.registerCatalysts(registration);
    }

}
