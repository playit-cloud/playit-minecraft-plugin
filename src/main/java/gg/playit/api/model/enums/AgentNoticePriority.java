package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentNoticePriority {
    Critical("Critical"),
    High("High"),
    Low("Low");

    private final String value;

    AgentNoticePriority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
