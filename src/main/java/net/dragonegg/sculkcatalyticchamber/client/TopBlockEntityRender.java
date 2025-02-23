package net.dragonegg.sculkcatalyticchamber.client;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import net.dragonegg.sculkcatalyticchamber.content.block.entity.TopBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class TopBlockEntityRender extends KineticBlockEntityRenderer<TopBlockEntity> {

    public TopBlockEntityRender(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(TopBlockEntity te, BlockState state) {
        return CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.UP);
    }
}

