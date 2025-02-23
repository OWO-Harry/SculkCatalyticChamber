package net.dragonegg.sculkcatalyticchamber.content.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.dragonegg.sculkcatalyticchamber.Registry;
import net.dragonegg.sculkcatalyticchamber.content.block.entity.BottomBlockEntity;
import net.dragonegg.sculkcatalyticchamber.content.block.entity.TopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TopBlock extends KineticBlock implements IBEIOUsable<TopBlockEntity>, IWrenchable {

    public TopBlock(Properties pProperties) {
        super(pProperties);
    }

    private boolean hasBottom(BlockGetter level, BlockPos pos) {
        BlockState p = level.getBlockState(pos.below(2));
        return p.getBlock() instanceof BottomBlock;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.hasBottom(level, pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.IOUse(level, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState p_199600_1_, BlockGetter p_199600_2_, BlockPos p_199600_3_) {
        return Block.box(0.0F, 0.0F, 0.0F, 16.0F, 14.0F, 16.0F);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, worldIn, pos, newState);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
                                  LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        return !canSurvive(pState, pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : pState;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player pPlayer) {
        super.playerWillDestroy(level, pos, state, pPlayer);
        if (this.hasBottom(level, pos)) {
            level.destroyBlock(pos.below(2), !pPlayer.isCreative());
        }
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return Direction.Axis.Y;
    }

    @Override
    public IRotate.SpeedLevel getMinimumRequiredSpeedLevel() {
        //TODO: get from config
        return IRotate.SpeedLevel.valueOf("none");
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return this.hasBottom(level, pos)? Registry.BOTTOM_BLOCK.asStack() : ItemStack.EMPTY;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return this.getBlockEntityOptional(worldIn, pos)
                .map(TopBlockEntity::getInventory)
                .map(ItemHelper::calcRedstoneFromInventory).orElse(0);
    }

    @Override
    public Class<TopBlockEntity> getBlockEntityClass() {
        return TopBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TopBlockEntity> getBlockEntityType() {
        return Registry.TOP_BLOCK_TILE.get();
    }
}
