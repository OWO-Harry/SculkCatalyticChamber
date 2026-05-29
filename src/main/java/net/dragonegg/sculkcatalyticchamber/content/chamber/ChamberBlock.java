package net.dragonegg.sculkcatalyticchamber.content.chamber;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.dragonegg.sculkcatalyticchamber.registry.BlockRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
public abstract class ChamberBlock<T extends ChamberBlockEntity> extends Block implements IBE<T>, IWrenchable {

    public static final Set<BlockPos> breakPos = new HashSet<>();

    public ChamberBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        return this.onBlockEntityUse(worldIn, pos, be -> be.use(worldIn, pos, player, InteractionHand.MAIN_HAND));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
                                              Player player, InteractionHand handIn, BlockHitResult hit) {
        InteractionResult result = this.onBlockEntityUse(worldIn, pos, be -> be.use(worldIn, pos, player, handIn));
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.sidedSuccess(worldIn.isClientSide);
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> ItemInteractionResult.FAIL;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        return this.onBlockEntityUse(worldIn, pos, be -> be.use(worldIn, pos, player, handIn));
    }

    @Override
    protected boolean isPathfindable(BlockState pState, PathComputationType pType) {
        return false;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return ordinal() == 0 || level.getBlockState(pos.below(ordinal())).getBlock() instanceof ChamberBottomBlock;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            breakMultiblock(state, level, pos, null);
            IBE.onRemove(state, level, pos, newState);
        }
    }

    public static void breakMultiblock(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Player player) {
        if (!breakPos.contains(pos) && state.getBlock() instanceof ChamberBlock<?> chamber) {
            breakPos.add(pos);
            for (int i = 0; i < 3; i++) {
                BlockPos posI = pos.above(i - chamber.ordinal());
                BlockState stateI = level.getBlockState(posI);
                if (stateI.getBlock() instanceof ChamberBlock<?> && !breakPos.contains(posI)) {
                    breakPos.add(posI);
                    level.destroyBlock(posI, player == null || !player.isCreative(), player);
                    breakPos.remove(posI);
                }
            }
            breakPos.remove(pos);
        }
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        for (int i = 0; i < 3; i++) {
            BlockPos posI = pos.above(i - ordinal());
            BlockState stateI = level.getBlockState(posI);
            if (stateI.getBlock() instanceof ChamberBlock<?>) {
                level.destroyBlock(posI, true);
            }
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        for (int i = 0; i < 3; i++) {
            BlockPos posI = pos.above(i - ordinal());
            BlockState stateI = level.getBlockState(posI);
            if (stateI.getBlock() instanceof ChamberBlock<?> chamber) {
                chamber.wasExploded(level, posI, explosion);
            }
            level.setBlock(posI, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (!(world instanceof ServerLevel serverLevel))
            return InteractionResult.SUCCESS;

        for (int i = 0; i < 3; i++) {
            BlockPos posI = pos.above(i - ordinal());
            BlockState stateI = world.getBlockState(posI);

            if (player != null && !player.isCreative()) {
                Block.getDrops(stateI, serverLevel, posI, world.getBlockEntity(posI), player, context.getItemInHand())
                        .forEach(itemStack -> player.getInventory().placeItemBackInInventory(itemStack));
            }
            stateI.spawnAfterBreak(serverLevel, posI, ItemStack.EMPTY, true);
            world.destroyBlock(posI, false);
        }

        IWrenchable.playRemoveSound(world, pos);
        return InteractionResult.SUCCESS;
    }

    public static boolean isChamber(LevelReader world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof ChamberBlockEntity;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return BlockRegistry.CHAMBER_BOTTOM_BLOCK.asStack();
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return this.getBlockEntityOptional(worldIn, pos)
                .map(ChamberBlockEntity::getInputInventory)
                .map(ItemHelper::calcRedstoneFromInventory).orElse(0);
    }

    public abstract int ordinal();

}
