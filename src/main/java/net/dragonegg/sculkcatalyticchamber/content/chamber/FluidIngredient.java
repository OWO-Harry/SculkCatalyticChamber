package net.dragonegg.sculkcatalyticchamber.content.chamber;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.Optional;

public class FluidIngredient {
    public static final FluidIngredient EMPTY = new FluidIngredient(null, null, 0);
    public static final Codec<FluidIngredient> CODEC = RecordCodecBuilder.<FluidIngredient>create(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid").forGetter(ingredient -> Optional.ofNullable(ingredient.fluid)),
            ResourceLocation.CODEC.optionalFieldOf("fluidTag").forGetter(ingredient -> ingredient.tag == null ? Optional.empty() : Optional.of(ingredient.tag.location())),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(FluidIngredient::getRequiredAmount)
    ).apply(instance, (fluid, tag, amount) -> fluid
            .map(value -> fromFluid(value, amount))
            .orElseGet(() -> tag
                    .map(value -> fromTag(TagKey.create(Registries.FLUID, value), amount))
                    .orElse(EMPTY)))).validate(ingredient -> ingredient == EMPTY
            ? DataResult.error(() -> "Expected a fluid ingredient with 'fluid' or 'fluidTag'")
            : DataResult.success(ingredient));

    private final Fluid fluid;
    private final TagKey<Fluid> tag;
    private final int amount;

    private FluidIngredient(Fluid fluid, TagKey<Fluid> tag, int amount) {
        this.fluid = fluid;
        this.tag = tag;
        this.amount = amount;
    }

    public static FluidIngredient fromFluid(Fluid fluid, int amount) {
        return new FluidIngredient(fluid, null, amount);
    }

    public static FluidIngredient fromTag(TagKey<Fluid> tag, int amount) {
        return new FluidIngredient(null, tag, amount);
    }

    public static boolean isFluidIngredient(JsonElement element) {
        if (!element.isJsonObject())
            return false;
        JsonObject object = element.getAsJsonObject();
        return object.has("fluid") || object.has("fluidTag");
    }

    public static FluidIngredient deserialize(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        int amount = GsonHelper.getAsInt(object, "amount", 0);
        if (object.has("fluid")) {
            Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(GsonHelper.getAsString(object, "fluid")));
            return fromFluid(fluid, amount);
        }
        ResourceLocation tagId = ResourceLocation.parse(GsonHelper.getAsString(object, "fluidTag"));
        return fromTag(TagKey.create(Registries.FLUID, tagId), amount);
    }

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("amount", amount);
        if (fluid != null)
            object.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        if (tag != null)
            object.addProperty("fluidTag", tag.location().toString());
        return object;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(fluid != null);
        buffer.writeResourceLocation(fluid != null ? BuiltInRegistries.FLUID.getKey(fluid) : tag.location());
        buffer.writeVarInt(amount);
    }

    public static FluidIngredient read(FriendlyByteBuf buffer) {
        boolean isFluid = buffer.readBoolean();
        ResourceLocation id = buffer.readResourceLocation();
        int amount = buffer.readVarInt();
        if (isFluid)
            return fromFluid(BuiltInRegistries.FLUID.get(id), amount);
        return fromTag(TagKey.create(Registries.FLUID, id), amount);
    }

    public boolean test(FluidStack stack) {
        if (stack.isEmpty())
            return false;
        if (fluid != null)
            return stack.getFluid() == fluid;
        return stack.getFluid().builtInRegistryHolder().is(tag);
    }

    public int getRequiredAmount() {
        return amount;
    }

    public FluidStack[] getMatchingFluidStacks() {
        if (fluid != null)
            return new FluidStack[] { new FluidStack(fluid, amount) };
        return BuiltInRegistries.FLUID.getTag(tag)
                .map(holders -> holders.stream()
                        .map(holder -> new FluidStack(holder.value(), amount))
                        .toArray(FluidStack[]::new))
                .orElseGet(() -> Arrays.copyOf(new FluidStack[0], 0));
    }
}
