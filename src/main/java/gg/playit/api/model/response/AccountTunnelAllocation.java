package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "status")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AccountTunnelAllocation.Pending.class, name = "pending"),
    @JsonSubTypes.Type(value = AccountTunnelAllocation.Disabled.class, name = "disabled"),
    @JsonSubTypes.Type(value = AccountTunnelAllocation.Allocated.class, name = "allocated")
})
public sealed interface AccountTunnelAllocation permits AccountTunnelAllocation.Pending, AccountTunnelAllocation.Disabled, AccountTunnelAllocation.Allocated {
    record Pending(Object data) implements AccountTunnelAllocation {}
    record Disabled(TunnelDisabled data) implements AccountTunnelAllocation {}
    record Allocated(TunnelAllocated data) implements AccountTunnelAllocation {}
}
