package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "resource")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConnectAddressSource.PortAllocation.class, name = "port-allocation"),
    @JsonSubTypes.Type(value = ConnectAddressSource.Gateway.class, name = "gateway")
})
public sealed interface ConnectAddressSource permits ConnectAddressSource.PortAllocation, ConnectAddressSource.Gateway {
    record PortAllocation(String id) implements ConnectAddressSource {}
    record Gateway(String id) implements ConnectAddressSource {}
}
