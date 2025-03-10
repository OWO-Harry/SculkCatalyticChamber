package net.dragonegg.sculkcatalyticchamber;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.dragonegg.sculkcatalyticchamber.registry.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SculkCatalyticChamber.MODID)
public class SculkCatalyticChamber {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "sculkcatalyticchamber";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    public SculkCatalyticChamber() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        REGISTRATE.registerEventListeners(modEventBus);

        CreativeModTabRegistry.register(modEventBus);
        BlockRegistry.register();
        RecipeRegistry.register(modEventBus);
        ParticleTypeRegistry.register(modEventBus);

        SCCConfig.register();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> onClientInit(modEventBus));

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void onClientInit(IEventBus modEventBus) {
        // Some client setup code
        PartialModelRegistry.init();
        SpriteShiftRegistry.init();
        modEventBus.addListener(ParticleTypeRegistry::registerFactories);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
