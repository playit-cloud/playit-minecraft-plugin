package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TunnelCreateUseAllocation.DedicatedIp.class, name = "dedicated-ip"),
    @JsonSubTypes.Type(value = TunnelCreateUseAllocation.PortAllocation.class, name = "port-allocation"),
    @JsonSubTypes.Type(value = TunnelCreateUseAllocation.Region.class, name = "region")
})
public sealed interface TunnelCreateUseAllocation permits TunnelCreateUseAllocation.DedicatedIp, TunnelCreateUseAllocation.PortAllocation, TunnelCreateUseAllocation.Region {
    record DedicatedIp(@NotNull UseAllocDedicatedIp details) implements TunnelCreateUseAllocation {}
    record PortAllocation(@NotNull UseAllocPortAlloc details) implements TunnelCreateUseAllocation {}
    record Region(@NotNull UseRegion details) implements TunnelCreateUseAllocation {}
}
