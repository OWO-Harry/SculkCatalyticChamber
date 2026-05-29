package net.dragonegg.sculkcatalyticchamber.compat.jei;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
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
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import static mezz.jei.api.recipe.RecipeType.createRecipeHolderType;
import static net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber.MODID;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class SculkCatalyticChamberJEI implements IModPlugin {

    private static final ResourceLocation ID = SculkCatalyticChamber.asResource("jei_plugin");
    private static final RecipeType<RecipeHolder<ChamberRecipe>> CHAMBER_TYPE =
            createRecipeHolderType(SculkCatalyticChamber.asResource("chamber"));

    private CreateRecipeCategory<ChamberRecipe> chamber;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    private void loadCategory() {
        Supplier<ItemStack> supplier1 = () -> new ItemStack(BlockRegistry.CHAMBER_BOTTOM_BLOCK.get());
        Supplier<ItemStack> supplier2 = () -> new ItemStack(BlockRegistry.MECHANICAL_SHRIEKER_BLOCK.get());

        Component title = Component.translatable(MODID + ".recipe.chamber");
        IDrawable background = new EmptyBackground(193, 163);
        IDrawable icon = new DoubleItemIcon(supplier1, supplier2);
        List<Supplier<? extends ItemStack>> catalysts = List.of(supplier1, supplier2);

        chamber = new ChamberCategory(new CreateRecipeCategory.Info<>(
                CHAMBER_TYPE,
                title,
                background,
                icon,
                this::getRecipes,
                catalysts
        ));
    }

    private List<RecipeHolder<ChamberRecipe>> getRecipes() {
        List<RecipeHolder<ChamberRecipe>> recipes = new ArrayList<>();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            recipes.addAll(connection.getRecipeManager().getAllRecipesFor(RecipeRegistry.CHAMBER.getType()));
        }
        if (recipes.isEmpty()) {
            recipes.addAll(loadBuiltinRecipesForJei());
        }
        return recipes;
    }

    private List<RecipeHolder<ChamberRecipe>> loadBuiltinRecipesForJei() {
        List<RecipeHolder<ChamberRecipe>> recipes = new ArrayList<>();
        Minecraft.getInstance().getResourceManager()
                .listResources("recipe/chamber", resourceLocation ->
                        resourceLocation.getNamespace().equals(MODID) && resourceLocation.getPath().endsWith(".json"))
                .forEach((id, resource) -> readRecipe(id, resource, recipes));
        return recipes;
    }

    private void readRecipe(ResourceLocation id, Resource resource, List<RecipeHolder<ChamberRecipe>> recipes) {
        try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
            JsonElement json = JsonParser.parseReader(reader);
            ((net.minecraft.world.item.crafting.RecipeSerializer<ChamberRecipe>) RecipeRegistry.CHAMBER.getSerializer()).codec().codec()
                    .parse(JsonOps.INSTANCE, json)
                    .result()
                    .map(recipe -> new RecipeHolder<>(toRecipeId(id), recipe))
                    .ifPresent(recipes::add);
        } catch (Exception ignored) {
        }
    }

    private ResourceLocation toRecipeId(ResourceLocation resourceId) {
        String path = resourceId.getPath();
        if (path.startsWith("recipe/")) {
            path = path.substring("recipe/".length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - ".json".length());
        }
        return ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(), path);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategory();
        registration.addRecipeCategories(chamber);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (chamber == null) {
            loadCategory();
        }
        chamber.registerRecipes(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (chamber == null) {
            loadCategory();
        }
        chamber.registerCatalysts(registration);
    }
}
