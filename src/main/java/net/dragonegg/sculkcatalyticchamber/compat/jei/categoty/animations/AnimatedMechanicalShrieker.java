package net.dragonegg.sculkcatalyticchamber.compat.jei.categoty.animations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.dragonegg.sculkcatalyticchamber.registry.BlockRegistry;
import net.dragonegg.sculkcatalyticchamber.registry.PartialModelRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class AnimatedMechanicalShrieker extends AnimatedKinetics {

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 300);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;
        double h = 1.0;

        blockElement(PartialModelRegistry.MECHANICAL_SHRIEKER_COG)
                .rotateBlock(180, getCurrentAngle() * 2, 0)
                .atLocal(0, 0, 0)
                .scale(scale)
                .render(graphics);

        blockElement(BlockRegistry.MECHANICAL_SHRIEKER_BLOCK.getDefaultState().setValue(FACING, Direction.DOWN))
                .atLocal(0, 0, 0)
                .scale(scale)
                .render(graphics);

        blockElement(BlockRegistry.CHAMBER_TOP_BLOCK.getDefaultState())
                .atLocal(0, 2 * h, 0)
                .scale(scale)
                .render(graphics);

        blockElement(BlockRegistry.CHAMBER_MIDDLE_BLOCK.getDefaultState())
                .atLocal(0, 3 * h, 0)
                .scale(scale)
                .render(graphics);

        blockElement(BlockRegistry.CHAMBER_BOTTOM_BLOCK.getDefaultState())
                .atLocal(0, 4 * h, 0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }

}
