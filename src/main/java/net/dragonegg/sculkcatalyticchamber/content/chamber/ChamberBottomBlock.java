package net.dragonegg.sculkcatalyticchamber.content.chamber;

import com.simibubi.create.AllShapes;
import net.dragonegg.sculkcatalyticchamber.registry.BlockRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
public class ChamberBottomBlock extends ChamberBlock<ChamberBottomBlockEntity> {

    public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;

    public ChamberBottomBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.DOWN));
    }

    @Override
    public int ordinal() {
        return 0;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_206840_1_) {
        super.createBlockStateDefinition(p_206840_1_.add(FACING));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (!context.getLevel().isClientSide)
            withBlockEntityDo(context.getLevel(), context.getClickedPos(),
                    bte -> bte.onWrenched(context.getClickedFace()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter getter, BlockPos pos) {
        return AllShapes.BASIN_RAYTRACE_SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.BASIN_BLOCK_SHAPE;
    }

    @Override
    public Class<ChamberBottomBlockEntity> getBlockEntityClass() {
        return ChamberBottomBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ChamberBottomBlockEntity> getBlockEntityType() {
        return BlockRegistry.CHAMBER_BOTTOM_BLOCK_TILE.get();
    }

}
