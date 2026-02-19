package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PublicAllocation.PortAllocationType.class, name = "PortAllocation"),
    @JsonSubTypes.Type(value = PublicAllocation.GatewayType.class, name = "Gateway")
})
public sealed interface PublicAllocation permits PublicAllocation.PortAllocationType, PublicAllocation.GatewayType {
    record PortAllocationType(PortAllocation details) implements PublicAllocation {}
    record GatewayType(GatewayAllocation details) implements PublicAllocation {}
}
