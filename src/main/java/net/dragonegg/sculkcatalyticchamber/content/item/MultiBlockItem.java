package net.dragonegg.sculkcatalyticchamber.content.item;

import net.dragonegg.sculkcatalyticchamber.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MultiBlockItem extends BlockItem {

    public MultiBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext pContext, BlockState pState) {
        BlockPos place = pContext.getClickedPos();
        for(int y = 0; y < 3; y++) {
            BlockPos pos = place.above(y);
            if(pContext.getLevel().isOutsideBuildHeight(pos)) return false;
            BlockPlaceContext p = BlockPlaceContext.at(pContext, pos.below(), Direction.UP);
            if(p.replacingClickedOnBlock()) {
                p = BlockPlaceContext.at(pContext, pos, Direction.UP);
                if(!p.replacingClickedOnBlock()) return false;
            }
            if(!p.canPlace()) return false;
            if(!super.canPlace(p, pState)) return false;
        }
        return true;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext pContext, BlockState pState) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        level.setBlock(pos, pState, 11);
        pos = pos.above();
        level.setBlock(pos, Registry.MIDDLE_BLOCK.getDefaultState(), 11);
        pos = pos.above();
        level.setBlock(pos, Registry.TOP_BLOCK.getDefaultState(), 11);
        return true;
    }
}
