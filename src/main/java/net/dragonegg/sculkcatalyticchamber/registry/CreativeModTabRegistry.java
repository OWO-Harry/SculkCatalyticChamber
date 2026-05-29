package net.dragonegg.sculkcatalyticchamber.registry;

import com.simibubi.create.AllCreativeModeTabs;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import static net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber.MODID;

public class CreativeModTabRegistry {

    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = REGISTER.register("base",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MODID))
                    .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey())
                    .icon(Items.ECHO_SHARD::getDefaultInstance)
                    .displayItems((para, out) -> {
                        out.accept(BlockRegistry.CHAMBER_BOTTOM_BLOCK.asItem());
                        out.accept(BlockRegistry.MECHANICAL_SHRIEKER_BLOCK.asItem());
                    })
                    .build()
    );

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

}
