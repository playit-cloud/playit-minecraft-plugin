package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Platform {
    Linux("linux"),
    Freebsd("freebsd"),
    Windows("windows"),
    Macos("macos"),
    Android("android"),
    Ios("ios"),
    Docker("docker"),
    MinecraftPlugin("minecraft-plugin"),
    Unknown("unknown");

    private final String value;

    Platform(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
