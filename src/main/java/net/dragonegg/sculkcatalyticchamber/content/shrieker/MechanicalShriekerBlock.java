package net.dragonegg.sculkcatalyticchamber.content.shrieker;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VoxelShaper;
import net.dragonegg.sculkcatalyticchamber.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MechanicalShriekerBlock extends DirectionalKineticBlock implements IBE<MechanicalShriekerBlockEntity>, ICogWheel {
    public static final BooleanProperty SHRIEKING = BlockStateProperties.SHRIEKING;

    private static final VoxelShaper SHAPER = new AllShapes.Builder(Block.box(0,0,0,16,6,16)).add(Block.box(2,6,2,14,10,14)).forDirectional();

    public MechanicalShriekerBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState().setValue(SHRIEKING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHRIEKING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.MEDIUM;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.NORMAL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPER.get(state.getValue(FACING));
    }

    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public Class<MechanicalShriekerBlockEntity> getBlockEntityClass() {
        return MechanicalShriekerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalShriekerBlockEntity> getBlockEntityType() {
        return BlockRegistry.MECHANICAL_SHRIEKER_BLOCK_TILE.get();
    }
}
