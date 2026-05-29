package net.dragonegg.sculkcatalyticchamber.content.chamber;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.createmod.catnip.render.SpriteShiftEntry;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.IntAttached;
import net.createmod.catnip.math.VecHelper;
import net.dragonegg.sculkcatalyticchamber.registry.SpriteShiftRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static net.dragonegg.sculkcatalyticchamber.content.chamber.ChamberBottomBlockEntity.OUTPUT_ANIMATION_TIME;

public class ChamberBottomRenderer extends SmartBlockEntityRenderer<ChamberBottomBlockEntity> {

    public ChamberBottomRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ChamberBottomBlockEntity chamber, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(chamber, partialTicks, ms, buffer, light, overlay);

        Level level = chamber.getLevel();
        if (level == null) return;

        BlockState blockState = chamber.getBlockState();
        BlockPos pos = chamber.getBlockPos();
        if (!(blockState.getBlock() instanceof ChamberBottomBlock)) return;

        Direction direction = blockState.getValue(ChamberBottomBlock.FACING);
        if (direction != Direction.DOWN) {
            Vec3 directionVec = Vec3.atLowerCornerOf(direction.getNormal());
            Vec3 outVec = VecHelper.getCenterOf(BlockPos.ZERO)
                    .add(directionVec.scale(.55).subtract(0, 1 / 2f, 0));

            boolean outToBasin =
                    level.getBlockState(pos.relative(direction).below()).getBlock() instanceof BasinBlock;

            for (IntAttached<ItemStack> intAttached : chamber.visualizedOutputItems) {
                float progress = 1 - (intAttached.getFirst() - partialTicks) / OUTPUT_ANIMATION_TIME;

                if (!outToBasin && progress > .35f)
                    continue;

                ms.pushPose();
                TransformStack.of(ms)
                        .translate(outVec)
                        .translate(new Vec3(0, Math.max(-.55f, -(progress * progress * 2)), 0))
                        .translate(directionVec.scale(progress * .5f))
                        .rotateY(AngleHelper.horizontalAngle(direction))
                        .rotateX(progress * 180);
                renderItem(ms, buffer, light, overlay, intAttached.getValue());
                ms.popPose();
            }
        }

        BlockState belowState = level.getBlockState(pos.below());
        BlockEntity belowBE = level.getBlockEntity(pos.below());
        if (belowBE instanceof BlazeBurnerBlockEntity burner) {
            HeatLevel heatLevel = burner.getHeatLevelFromBlock();
            if (heatLevel.isAtLeast(HeatLevel.FADING)) {
                float headAngle = (AngleHelper.horizontalAngle(Direction.SOUTH) + 180) % 360;
                float horizontalAngle = AngleHelper.rad(headAngle);

                ms.pushPose();
                ms.translate(0.0F, -1.0F, 0.0F);
                renderFlame(ms, buffer, level, belowState, heatLevel, horizontalAngle);
                ms.popPose();
            }
        }
    }

    protected void renderItem(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        mc.getItemRenderer()
                .renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, ms, buffer, mc.level, 0);
    }

    protected void renderFlame(PoseStack ms, MultiBufferSource bufferSource, Level level,
                               BlockState blockState, HeatLevel heatLevel, float horizontalAngle) {
        float time = AnimationTickHolder.getRenderTime(level);
        VertexConsumer cutout = bufferSource.getBuffer(RenderType.cutoutMipped());

        SpriteShiftEntry spriteShift = SpriteShiftRegistry.SCULK_FLAME;

        float spriteWidth = spriteShift.getTarget().getU1() - spriteShift.getTarget().getU0();
        float spriteHeight = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();
        float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();

        double vScroll = speed * time;
        vScroll = vScroll - Math.floor(vScroll);
        vScroll = vScroll * spriteHeight / 2;

        double uScroll = speed * time / 2;
        uScroll = uScroll - Math.floor(uScroll);
        uScroll = uScroll * spriteWidth / 2;

        SuperByteBuffer flameBuffer = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, blockState);
        flameBuffer.shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll);
        draw(flameBuffer, horizontalAngle, ms, cutout);
    }

    private static void draw(SuperByteBuffer buffer, float horizontalAngle, PoseStack ms, VertexConsumer vc) {
        buffer.rotateCentered(horizontalAngle, Direction.UP)
                .light(LightTexture.FULL_BRIGHT)
                .renderInto(ms, vc);
    }

    @Override
    public int getViewDistance() {
        return 16;
    }
}
