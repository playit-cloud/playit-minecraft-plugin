package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gg.playit.api.model.enums.TunnelType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "name")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AgentSchemaTunnelType.CustomTcp.class, name = "custom-tcp"),
    @JsonSubTypes.Type(value = AgentSchemaTunnelType.CustomUdp.class, name = "custom-udp"),
    @JsonSubTypes.Type(value = AgentSchemaTunnelType.CustomBoth.class, name = "custom-both"),
    @JsonSubTypes.Type(value = AgentSchemaTunnelType.TunnelTypeDetail.class, name = "tunnel-type")
})
public sealed interface AgentSchemaTunnelType permits AgentSchemaTunnelType.CustomTcp, AgentSchemaTunnelType.CustomUdp, AgentSchemaTunnelType.CustomBoth, AgentSchemaTunnelType.TunnelTypeDetail {
    record CustomTcp(AgentTunnelTypeSupportedPorts details) implements AgentSchemaTunnelType {}
    record CustomUdp(AgentTunnelTypeSupportedPorts details) implements AgentSchemaTunnelType {}
    record CustomBoth(AgentTunnelTypeSupportedPorts details) implements AgentSchemaTunnelType {}
    record TunnelTypeDetail(TunnelType details) implements AgentSchemaTunnelType {}
}
