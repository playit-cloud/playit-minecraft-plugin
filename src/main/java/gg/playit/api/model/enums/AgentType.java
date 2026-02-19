package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentType {
    Default("default"),
    Assignable("assignable"),
    SelfManaged("self-managed");

    private final String value;

    AgentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
