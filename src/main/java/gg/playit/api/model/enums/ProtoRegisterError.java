package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProtoRegisterError {
    UnknownPlayitVersion("UnknownPlayitVersion"),
    DisabledByUser("DisabledByUser"),
    AgentDisabledOverLimit("AgentDisabledOverLimit"),
    AccountBanned("AccountBanned");

    private final String value;

    ProtoRegisterError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
