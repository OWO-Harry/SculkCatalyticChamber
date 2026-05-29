package net.dragonegg.sculkcatalyticchamber.registry;

import com.simibubi.create.Create;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;
import net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber;

public class SpriteShiftRegistry {

    public static final SpriteShiftEntry SCULK_FLAME = SpriteShifter.get(
            Create.asResource("block/blaze_burner_flame"),
            SculkCatalyticChamber.asResource("block/blaze_burner_flame_sculk_scroll"));

    public static void init() {
        // init static fields
    }

}
