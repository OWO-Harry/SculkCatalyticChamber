package net.dragonegg.sculkcatalyticchamber.events;

import net.dragonegg.sculkcatalyticchamber.content.chamber.ChamberBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

import static net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber.MODID;

@EventBusSubscriber(modid = MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        if (state.getBlock() instanceof ChamberBlock<?>) {
            ChamberBlock.breakMultiblock(state, level, pos, event.getPlayer());
        }
    }

}
