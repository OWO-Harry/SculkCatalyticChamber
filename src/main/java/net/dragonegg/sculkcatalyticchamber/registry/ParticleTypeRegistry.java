package net.dragonegg.sculkcatalyticchamber.registry;

import net.dragonegg.sculkcatalyticchamber.content.shrieker.MechanicalShriekerParticleData;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import static net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber.MODID;
import static net.dragonegg.sculkcatalyticchamber.content.shrieker.MechanicalShriekerParticleData.FACTORY;

public class ParticleTypeRegistry {

    private static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);

    public static DeferredHolder<ParticleType<?>, ParticleType<MechanicalShriekerParticleData>> MECHANICAL_SHRIEKER =
            REGISTER.register("mechanical_shrieker", () -> FACTORY.get().createType());

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerFactories(RegisterParticleProvidersEvent event) {
        FACTORY.get().register(MECHANICAL_SHRIEKER.get(), event);
    }

}
