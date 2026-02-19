package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelCreateError {
    DefaultAgentNotSupported("DefaultAgentNotSupported"),
    AgentNotFound("AgentNotFound"),
    InvalidAgentId("InvalidAgentId"),
    AgentVersionTooOld("AgentVersionTooOld"),
    DedicatedIpNotFound("DedicatedIpNotFound"),
    DedicatedIpPortNotAvailable("DedicatedIpPortNotAvailable"),
    DedicatedIpNotEnoughSpace("DedicatedIpNotEnoughSpace"),
    PortAllocNotFound("PortAllocNotFound"),
    InvalidIpHostname("InvalidIpHostname"),
    ManagedMissingAgentId("ManagedMissingAgentId"),
    InvalidPortCount("InvalidPortCount"),
    RequiresVerifiedAccount("RequiresVerifiedAccount"),
    InvalidTunnelName("InvalidTunnelName"),
    FirewallNotFound("FirewallNotFound"),
    AllocInvalid("AllocInvalid"),
    InvalidOrigin("InvalidOrigin"),
    RequiresPlayitPremium("RequiresPlayitPremium"),
    TunnelTypeBlockedOnRegion("TunnelTypeBlockedOnRegion"),
    TunnelTypeRequiresDescription("TunnelTypeRequiresDescription"),
    Other("Other");

    private final String value;

    TunnelCreateError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
