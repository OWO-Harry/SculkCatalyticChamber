package net.dragonegg.sculkcatalyticchamber;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.dragonegg.sculkcatalyticchamber.client.TopBlockEntityRender;
import net.dragonegg.sculkcatalyticchamber.content.block.BottomBlock;
import net.dragonegg.sculkcatalyticchamber.content.block.MiddleBlock;

import net.dragonegg.sculkcatalyticchamber.content.block.TopBlock;
import net.dragonegg.sculkcatalyticchamber.content.block.entity.BottomBlockEntity;
import net.dragonegg.sculkcatalyticchamber.content.block.entity.MiddleBlockEntity;
import net.dragonegg.sculkcatalyticchamber.content.block.entity.TopBlockEntity;
import net.dragonegg.sculkcatalyticchamber.content.block.entity.TopInstance;
import net.dragonegg.sculkcatalyticchamber.content.item.MultiBlockItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.PushReaction;

public class Registry {

    private static final CreateRegistrate REGISTRATE = SculkCatalyticChamber.registrate();

    public static final RegistryEntry<CreativeModeTab> TAB = REGISTRATE.defaultCreativeTab("create_ore_excavation",
            c -> c.icon(() -> new ItemStack(Registry.BOTTOM_BLOCK.get()))
    ).register();

    public static final BlockEntry<TopBlock> TOP_BLOCK = REGISTRATE
            .block("kinetic_input", TopBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noLootTable().pushReaction(PushReaction.BLOCK))
            .tag(BlockTags.NEEDS_STONE_TOOL)
            .transform(TagGen.pickaxeOnly())
            .register();

    public static final BlockEntityEntry<TopBlockEntity> TOP_BLOCK_TILE = REGISTRATE
            .blockEntity("kinetic_input", TopBlockEntity::new)
            .instance(() -> TopInstance::new)
            .validBlocks(TOP_BLOCK)
            .renderer(() -> TopBlockEntityRender::new)
            .register();

    public static final BlockEntry<MiddleBlock> MIDDLE_BLOCK = REGISTRATE
            .block("catalyst_input", MiddleBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noLootTable().pushReaction(PushReaction.BLOCK))
            .tag(BlockTags.NEEDS_STONE_TOOL)
            .transform(TagGen.pickaxeOnly())
            .register();

    public static final BlockEntityEntry<MiddleBlockEntity> MIDDLE_BLOCK_TILE = REGISTRATE
            .blockEntity("catalyst_input", MiddleBlockEntity::new)
            .validBlocks(MIDDLE_BLOCK)
            .register();

    public static final BlockEntry<BottomBlock> BOTTOM_BLOCK = REGISTRATE
            .block("sculk_catalytic_chamber", BottomBlock::new)
            .initialProperties(SharedProperties::stone)
            .tag(BlockTags.NEEDS_STONE_TOOL)
            .transform(TagGen.pickaxeOnly())
            .item(MultiBlockItem::new)
            .transform(b -> b.model(
                    (c, p) ->
                            p.withExistingParent("sculk_catalytic_chamber", p.modLoc("block/sculk_catalytic_chamber"))).build())
            .register();

    public static final BlockEntityEntry<BottomBlockEntity> BOTTOM_BLOCK_TILE = REGISTRATE
            .blockEntity("sculk_catalytic_chamber", BottomBlockEntity::new)
            .validBlocks(BOTTOM_BLOCK)
            .register();

}
