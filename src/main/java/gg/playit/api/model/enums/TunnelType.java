package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelType {
    MinecraftJava("minecraft-java"),
    MinecraftBedrock("minecraft-bedrock"),
    Valheim("valheim"),
    Terraria("terraria"),
    Starbound("starbound"),
    Rust("rust"),
    SevenDays("7days"),
    Unturned("unturned"),
    Https("https"),
    Hytale("hytale"),
    ProjectZomboid("project-zomboid"),
    VintageStory("vintage-story");

    private final String value;

    TunnelType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
