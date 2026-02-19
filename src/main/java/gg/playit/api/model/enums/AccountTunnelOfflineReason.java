package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountTunnelOfflineReason {
    OriginNotSet("OriginNotSet"),
    AgentDisabled("AgentDisabled"),
    AgentOverLimit("AgentOverLimit"),
    TunnelDisabled("TunnelDisabled"),
    PublicAllocationMissing("PublicAllocationMissing"),
    PublicAllocationPending("PublicAllocationPending");

    private final String value;

    AccountTunnelOfflineReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
