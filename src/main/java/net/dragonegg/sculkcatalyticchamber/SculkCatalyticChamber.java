package net.dragonegg.sculkcatalyticchamber;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.dragonegg.sculkcatalyticchamber.registry.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SculkCatalyticChamber.MODID)
public class SculkCatalyticChamber {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "sculkcatalyticchamber";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    public SculkCatalyticChamber(IEventBus modEventBus) {
        REGISTRATE.registerEventListeners(modEventBus);

        CreativeModTabRegistry.register(modEventBus);
        BlockRegistry.register();
        BlockRegistry.registerCapabilities(modEventBus);
        RecipeRegistry.register(modEventBus);
        ParticleTypeRegistry.register(modEventBus);

        ArmInteractionPointTypeRegistry.register();

        SCCConfig.register();

        if (FMLEnvironment.dist == Dist.CLIENT)
            onClientInit(modEventBus);
    }

    public static void onClientInit(IEventBus modEventBus) {
        // Some client setup code
        PartialModelRegistry.init();
        SpriteShiftRegistry.init();
        modEventBus.addListener(ParticleTypeRegistry::registerFactories);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
