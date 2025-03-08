package net.dragonegg.sculkcatalyticchamber;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class SCCConfig {

    public SCCConfig() {}

    public static ForgeConfigSpec.ConfigValue<Integer> CHAMBER_SPEED;

    public static void register(){

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Sculk Catalytic Chamber Common Configs").push("common");
        CHAMBER_SPEED = builder.comment("Processing speed of chamber in ticks").define("speed", 100);

        builder.pop();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, builder.build());
    }

}
