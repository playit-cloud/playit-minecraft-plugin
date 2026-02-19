package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentAccountStatus {
    AccountDeleteScheduled("account-delete-scheduled"),
    Banned("banned"),
    HasMessage("has-message"),
    EmailNotVerified("email-not-verified"),
    Guest("guest"),
    Ready("ready"),
    AgentOverLimit("agent-over-limit"),
    AgentDisabled("agent-disabled");

    private final String value;

    AgentAccountStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
