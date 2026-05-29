package net.dragonegg.sculkcatalyticchamber.content.shrieker;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;
import static net.dragonegg.sculkcatalyticchamber.registry.PartialModelRegistry.MECHANICAL_SHRIEKER_COG;

public class MechanicalShriekerRender extends KineticBlockEntityRenderer<MechanicalShriekerBlockEntity> {

    public MechanicalShriekerRender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(MechanicalShriekerBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacingVertical(MECHANICAL_SHRIEKER_COG, state, state.getValue(FACING));
    }

}
