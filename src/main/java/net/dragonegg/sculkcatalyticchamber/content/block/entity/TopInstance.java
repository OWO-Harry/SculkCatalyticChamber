package net.dragonegg.sculkcatalyticchamber.content.block.entity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.kinetics.base.HalfShaftInstance;
import net.minecraft.core.Direction;

public class TopInstance extends HalfShaftInstance<TopBlockEntity> {
    public TopInstance(MaterialManager materialManager, TopBlockEntity blockEntity) {
        super(materialManager, blockEntity);
    }

    @Override
    protected Direction getShaftDirection() {
        return Direction.UP;
    }
}

