package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateTunnelEndpoint.Gateway.class, name = "gateway"),
    @JsonSubTypes.Type(value = CreateTunnelEndpoint.DedicatedIp.class, name = "dedicated-ip"),
    @JsonSubTypes.Type(value = CreateTunnelEndpoint.SharedIp.class, name = "shared-ip"),
    @JsonSubTypes.Type(value = CreateTunnelEndpoint.Region.class, name = "region"),
    @JsonSubTypes.Type(value = CreateTunnelEndpoint.PortAllocation.class, name = "port-allocation")
})
public sealed interface CreateTunnelEndpoint permits CreateTunnelEndpoint.Gateway, CreateTunnelEndpoint.DedicatedIp, CreateTunnelEndpoint.SharedIp, CreateTunnelEndpoint.Region, CreateTunnelEndpoint.PortAllocation {
    record Gateway(UseGateway details) implements CreateTunnelEndpoint {}
    record DedicatedIp(UseAllocDedicatedIp details) implements CreateTunnelEndpoint {}
    record SharedIp(UseAllocSharedIp details) implements CreateTunnelEndpoint {}
    record Region(UseAllocRegion details) implements CreateTunnelEndpoint {}
    record PortAllocation(String details) implements CreateTunnelEndpoint {}
}
