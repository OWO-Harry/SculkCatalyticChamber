package net.dragonegg.sculkcatalyticchamber;

public class SCCConfig {

    public SCCConfig() {}

    public static ConfigValue CHAMBER_SPEED = new ConfigValue(100);

    public static void register(){
    }

    public record ConfigValue(int value) {
        public int get() {
            return value;
        }
    }

}
