package net.dragonegg.sculkcatalyticchamber.content.block;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.dragonegg.sculkcatalyticchamber.Registry;
import net.dragonegg.sculkcatalyticchamber.content.block.entity.BottomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class BottomBlock extends Block implements IBEIOUsable<BottomBlockEntity>, IWrenchable {

    public static final DirectionProperty FACING;

    public BottomBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.DOWN));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_206840_1_) {
        super.createBlockStateDefinition(p_206840_1_.add(FACING));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.IOUse(level, pos, player, hand, hit);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, worldIn, pos, newState);
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
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return this.getBlockEntityOptional(worldIn, pos)
                .map(BottomBlockEntity::getInputInventory)
                .map(ItemHelper::calcRedstoneFromInventory).orElse(0);
    }

    @Override
    public Class<BottomBlockEntity> getBlockEntityClass() {
        return BottomBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BottomBlockEntity> getBlockEntityType() {
        return Registry.BOTTOM_BLOCK_TILE.get();
    }

    static {
        FACING = BlockStateProperties.FACING_HOPPER;
    }
}
