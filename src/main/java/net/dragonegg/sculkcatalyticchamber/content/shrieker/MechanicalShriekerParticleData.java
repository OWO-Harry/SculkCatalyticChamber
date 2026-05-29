package net.dragonegg.sculkcatalyticchamber.content.shrieker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import net.dragonegg.sculkcatalyticchamber.registry.ParticleTypeRegistry;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class MechanicalShriekerParticleData implements
        ParticleOptions, ICustomParticleDataWithSprite<MechanicalShriekerParticleData> {

    public static final MapCodec<MechanicalShriekerParticleData> CODEC = RecordCodecBuilder.mapCodec(i ->
            i.group(
                    Codec.INT.fieldOf("delay").forGetter(p -> p.delay),
                    Codec.INT.fieldOf("ordinal").forGetter(p -> p.ordinal())
            ).apply(i, MechanicalShriekerParticleData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MechanicalShriekerParticleData> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, data) -> {
                        buffer.writeInt(data.delay);
                        buffer.writeInt(data.ordinal());
                    },
                    buffer -> new MechanicalShriekerParticleData(buffer.readInt(), buffer.readInt()));

    public static final Supplier<MechanicalShriekerParticleData> FACTORY = MechanicalShriekerParticleData::new;

    private final int delay;
    private final Direction direction;

    public MechanicalShriekerParticleData(int delay, Direction direction) {
        this.delay = delay;
        this.direction = direction;
    }

    public MechanicalShriekerParticleData(int delay, int ordinal) {
        this(delay, Direction.values()[ordinal]);
    }

    public MechanicalShriekerParticleData() {
        this(0, Direction.UP);
    }

    public int getDelay() {
        return delay;
    }

    public Direction getDirection() {
        return direction;
    }

    public int ordinal() {
        return direction.ordinal();
    }

    @Override
    public ParticleType<?> getType() {
        return ParticleTypeRegistry.MECHANICAL_SHRIEKER.get();
    }

    @Override
    public MapCodec<MechanicalShriekerParticleData> getCodec(ParticleType<MechanicalShriekerParticleData> type) {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, MechanicalShriekerParticleData> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public ParticleEngine.SpriteParticleRegistration<MechanicalShriekerParticleData> getMetaFactory() {
        return MechanicalShriekerParticle.Factory::new;
    }
}
