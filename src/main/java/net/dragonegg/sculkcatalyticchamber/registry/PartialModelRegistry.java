package net.dragonegg.sculkcatalyticchamber.registry;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber;

public class PartialModelRegistry {

    public static final PartialModel MECHANICAL_SHRIEKER_COG =
            PartialModel.of(SculkCatalyticChamber.asResource("block/mechanical_shrieker/inner"));

    public static void init() {
        // init static fields
    }

}
