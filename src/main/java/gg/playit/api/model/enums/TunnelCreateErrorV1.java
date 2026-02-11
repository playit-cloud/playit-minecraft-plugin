package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelCreateErrorV1 {
    AgentNotFound("AgentNotFound"),
    InvalidAgentId("InvalidAgentId"),
    DedicatedIpNotFound("DedicatedIpNotFound"),
    PortAllocNotFound("PortAllocNotFound"),
    InvalidIpHostname("InvalidIpHostname"),
    InvalidPortCount("InvalidPortCount"),
    RequiresVerifiedAccount("RequiresVerifiedAccount"),
    RegionNotSupported("RegionNotSupported"),
    InvalidTunnelConfig("InvalidTunnelConfig"),
    FirewallNotFound("FirewallNotFound"),
    TunnelNameIsNotAscii("TunnelNameIsNotAscii"),
    TunnelNameTooLong("TunnelNameTooLong"),
    PortAllocDoesNotMatchPortDetails("PortAllocDoesNotMatchPortDetails"),
    RegionRequiresPlayitPremium("RegionRequiresPlayitPremium"),
    PortAllocCurrentlyAssigned("PortAllocCurrentlyAssigned"),
    PublicPortRequiresPlayitPremium("PublicPortRequiresPlayitPremium"),
    AgentVersionTooOld("AgentVersionTooOld"),
    RequiresPlayitPremium("RequiresPlayitPremium"),
    EndpointDoesNotSupportProtocol("EndpointDoesNotSupportProtocol"),
    InvalidGatewayId("InvalidGatewayId"),
    GatewayAlreadyHasTunnelType("GatewayAlreadyHasTunnelType"),
    GatewayDoesNotSupportTunnelType("GatewayDoesNotSupportTunnelType"),
    TunnelTypeBlockedOnRegion("TunnelTypeBlockedOnRegion"),
    InvalidSoftwareDescription("InvalidSoftwareDescription");

    private final String value;

    TunnelCreateErrorV1(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
