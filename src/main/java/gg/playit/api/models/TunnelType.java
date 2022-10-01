package gg.playit.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TunnelType {
    @JsonProperty("minecraft-java")
    MinecraftJava,
    @JsonProperty("minecraft-bedrock")
    MinecraftBedrock,
    @JsonProperty("valheim")
    Valheim,
    @JsonProperty("valheim")
    Terraria,
    @JsonProperty("starbound")
    Starbound,
    @JsonProperty("rust")
    Rust,
    @JsonProperty("7days")
    SevenDays,
    @JsonProperty("unturned")
    Unturned
}
