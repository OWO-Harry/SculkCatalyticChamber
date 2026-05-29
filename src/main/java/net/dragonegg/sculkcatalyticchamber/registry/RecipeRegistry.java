package net.dragonegg.sculkcatalyticchamber.registry;

import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber;
import net.dragonegg.sculkcatalyticchamber.content.chamber.ChamberRecipe;
import net.dragonegg.sculkcatalyticchamber.content.chamber.ChamberRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import static net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber.MODID;

public class RecipeRegistry {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);

    public static void register(IEventBus bus) {
        SERIALIZER_REGISTER.register(bus);
        TYPE_REGISTER.register(bus);
    }

    public static final IRecipeTypeInfo CHAMBER = new IRecipeTypeInfo() {
        private static final ResourceLocation id = SculkCatalyticChamber.asResource("chamber");
        private static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ChamberRecipe>> serializerObject =
                SERIALIZER_REGISTER.register("chamber", ChamberRecipeSerializer::new);
        private static final DeferredHolder<RecipeType<?>, RecipeType<ChamberRecipe>> typeObject =
                TYPE_REGISTER.register("chamber", () -> RecipeType.simple(id));

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @SuppressWarnings("unchecked")
        @Override
        public RecipeSerializer<ChamberRecipe> getSerializer() {
            return serializerObject.get();
        }

        @SuppressWarnings("unchecked")
        @Override
        public RecipeType<ChamberRecipe> getType() {
            return typeObject.get();
        }
    };

}
