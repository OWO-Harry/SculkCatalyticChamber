package net.dragonegg.sculkcatalyticchamber.registry;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.dragonegg.sculkcatalyticchamber.content.shrieker.*;
import net.dragonegg.sculkcatalyticchamber.content.chamber.*;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static net.dragonegg.sculkcatalyticchamber.SculkCatalyticChamber.REGISTRATE;

public class BlockRegistry {

    static {
        REGISTRATE.setCreativeTab(CreativeModTabRegistry.TAB);
    }

    public static final BlockEntry<ChamberTopBlock> CHAMBER_TOP_BLOCK = REGISTRATE
            .block("chamber_top", ChamberTopBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p
                    .mapColor(MapColor.COLOR_GRAY)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .pushReaction(PushReaction.BLOCK)
                    .noLootTable())
            .transform(TagGen.pickaxeOnly())
            .register();

    public static final BlockEntityEntry<ChamberTopBlockEntity> CHAMBER_TOP_BLOCK_TILE = REGISTRATE
            .blockEntity("chamber_top", ChamberTopBlockEntity::new)
            .validBlocks(CHAMBER_TOP_BLOCK)
            .register();

    public static final BlockEntry<ChamberMiddleBlock> CHAMBER_MIDDLE_BLOCK = REGISTRATE
            .block("chamber_middle", ChamberMiddleBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p
                    .mapColor(MapColor.COLOR_GRAY)
                    .pushReaction(PushReaction.BLOCK)
                    .noLootTable())
            .transform(TagGen.pickaxeOnly())
            .register();

    public static final BlockEntityEntry<ChamberMiddleBlockEntity> CHAMBER_MIDDLE_BLOCK_TILE = REGISTRATE
            .blockEntity("chamber_middle", ChamberMiddleBlockEntity::new)
            .validBlocks(CHAMBER_MIDDLE_BLOCK)
            .register();

    public static final BlockEntry<ChamberBottomBlock> CHAMBER_BOTTOM_BLOCK = REGISTRATE
            .block("chamber", ChamberBottomBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .pushReaction(PushReaction.BLOCK))
            .transform(TagGen.pickaxeOnly())
            .item(ChamberItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntityEntry<ChamberBottomBlockEntity> CHAMBER_BOTTOM_BLOCK_TILE = REGISTRATE
            .blockEntity("chamber", ChamberBottomBlockEntity::new)
            .validBlocks(CHAMBER_BOTTOM_BLOCK)
            .renderer(() -> ChamberBottomRenderer::new)
            .register();

    public static final BlockEntry<MechanicalShriekerBlock> MECHANICAL_SHRIEKER_BLOCK = REGISTRATE
            .block("mechanical_shrieker", MechanicalShriekerBlock::new)
            .initialProperties(() -> Blocks.SCULK_SHRIEKER)
            .transform(TagGen.axeOrPickaxe())
            .onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> 40.0))
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntityEntry<MechanicalShriekerBlockEntity> MECHANICAL_SHRIEKER_BLOCK_TILE = REGISTRATE
            .blockEntity("mechanical_shrieker", MechanicalShriekerBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual.of(PartialModelRegistry.MECHANICAL_SHRIEKER_COG), false)
            .validBlocks(MECHANICAL_SHRIEKER_BLOCK)
            .renderer(() -> MechanicalShriekerRender::new)
            .register();

    // Load this class

    public static void register() {}

    public static void registerCapabilities(IEventBus modEventBus) {
        modEventBus.addListener(BlockRegistry::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CHAMBER_TOP_BLOCK_TILE.get(),
                (be, side) -> be.getInvs());
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CHAMBER_TOP_BLOCK_TILE.get(),
                (be, side) -> be.getTanks());
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CHAMBER_MIDDLE_BLOCK_TILE.get(),
                (be, side) -> be.getInvs());
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CHAMBER_MIDDLE_BLOCK_TILE.get(),
                (be, side) -> be.getTanks());
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CHAMBER_BOTTOM_BLOCK_TILE.get(),
                (be, side) -> be.getInvs());
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CHAMBER_BOTTOM_BLOCK_TILE.get(),
                (be, side) -> be.getTanks());
    }

}
