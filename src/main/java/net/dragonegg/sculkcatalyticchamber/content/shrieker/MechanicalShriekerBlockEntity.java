package net.dragonegg.sculkcatalyticchamber.content.shrieker;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.VecHelper;
import net.dragonegg.sculkcatalyticchamber.SCCConfig;
import net.dragonegg.sculkcatalyticchamber.content.chamber.ChamberBlockEntity;
import net.dragonegg.sculkcatalyticchamber.content.chamber.ChamberOperatingBlockEntity;
import net.dragonegg.sculkcatalyticchamber.registry.RecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;
import static net.dragonegg.sculkcatalyticchamber.content.shrieker.MechanicalShriekerBlock.SHRIEKING;

public class MechanicalShriekerBlockEntity extends ChamberOperatingBlockEntity implements IHaveGoggleInformation {

    private static final Object shriekerCacheKey = new Object();

    public int runningTicks;
    public int processingTicks;
    public int renderingTicks;
    public boolean running;

    public MechanicalShriekerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        running = compound.getBoolean("Running");
        runningTicks = compound.getInt("Ticks");
        super.read(compound, clientPacket);
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.putBoolean("Running", running);
        compound.putInt("Ticks", runningTicks);
        super.write(compound, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

        if (runningTicks >= 40) {
            running = false;
            runningTicks = 0;
            chamberChecker.scheduleUpdate();
            return;
        }

        if (!running && level != null) {
            if (getBlockState().getValue(SHRIEKING))
                level.setBlock(worldPosition, getBlockState().setValue(SHRIEKING, false), 2);
        }

        float speed = Math.abs(getSpeed());
        if (running && level != null) {
            if (!getBlockState().getValue(SHRIEKING))
                level.setBlock(worldPosition, getBlockState().setValue(SHRIEKING, true), 2);

            if (level.isClientSide && renderingTicks == 20) {
                renderParticles(getBlockState().getValue(FACING));
                renderingTicks = 0;
            }

            if ((!level.isClientSide || isVirtual()) && runningTicks == 20) {
                if (processingTicks < 0) {
                    float recipeSpeed = 1;
                    int t = SCCConfig.CHAMBER_SPEED.get();
                    if (t != 0)
                        recipeSpeed = t / 100f;

                    processingTicks = Mth.clamp((Mth.log2((int) (512 / speed))) * Mth.ceil(recipeSpeed * 15) + 1, 1, 512);
                } else {
                    processingTicks--;
                    if (processingTicks == 0) {
                        runningTicks++;
                        processingTicks = -1;
                        applyChamberRecipe();
                        sendData();
                    }
                }
            }

            if (runningTicks != 20)
                runningTicks++;

            if (renderingTicks != 20)
                renderingTicks++;
        }
    }

    private void renderParticles(Direction direction) {
        Vec3 c = VecHelper.getCenterOf(getBlockPos());
        for(int j1 = 0; j1 < 4; ++j1) {
            level.addParticle(new MechanicalShriekerParticleData(j1 * 5, direction),
                    false, c.x(), c.y(), c.z(), 0.0D, 0.0D, 0.0D);
        }

        c = c.add(0, -3, 0);
        RandomSource r = level.random;
        for (int i = 0; i < 10; i++) {
            Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
                    .multiply(1, .25f, 1)
                    .normalize();
            Vec3 v = c.add(offset.scale(.5 + r.nextDouble() * .125f))
                    .add(0, .125, 0);
            Vec3 m = offset.scale(1 / 32f);

            level.addParticle(ParticleTypes.SCULK_SOUL, v.x, v.y, v.z, m.x, m.y, m.z);
        }
    }

    @Override
    protected boolean isRunning() {
        return running;
    }

    @Override
    public void startProcessingChamber() {
        if (running && runningTicks <= 20)
            return;
        super.startProcessingChamber();
        running = true;
        runningTicks = 0;
    }

    @Override
    public boolean continueWithPreviousRecipe() {
        runningTicks = 20;
        return true;
    }

    @Override
    protected void onChamberRemoved() {
        if (!running) return;
        runningTicks = 40;
        running = false;
    }

    @Override
    protected Optional<ChamberBlockEntity> getChamber() {
        if (level == null)
            return Optional.empty();
        BlockEntity be = level.getBlockEntity(worldPosition.below(2));
        if (!(be instanceof ChamberBlockEntity chamber))
            return Optional.empty();
        return Optional.of(chamber);
    }

    @Override
    protected <C extends Container> boolean matchStaticFilters(Recipe<C> recipe) {
        return recipe.getType() == RecipeRegistry.CHAMBER.getType();
    }

    @Override
    protected Object getRecipeCacheKey() {
        return shriekerCacheKey;
    }
}
