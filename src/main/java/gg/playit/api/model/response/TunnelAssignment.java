package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TunnelAssignment.DedicatedIp.class, name = "dedicated-ip"),
    @JsonSubTypes.Type(value = TunnelAssignment.SharedIp.class, name = "shared-ip"),
    @JsonSubTypes.Type(value = TunnelAssignment.DedicatedPort.class, name = "dedicated-port")
})
public sealed interface TunnelAssignment permits TunnelAssignment.DedicatedIp, TunnelAssignment.SharedIp, TunnelAssignment.DedicatedPort {
    record DedicatedIp(TunnelDedicatedIp subscription) implements TunnelAssignment {}
    record SharedIp(Object subscription) implements TunnelAssignment {}
    record DedicatedPort(SubscriptionId subscription) implements TunnelAssignment {}
}
