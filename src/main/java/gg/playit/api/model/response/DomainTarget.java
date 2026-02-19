package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = DomainTarget.IpAddress.class, name = "ip-address"),
    @JsonSubTypes.Type(value = DomainTarget.Tunnel.class, name = "tunnel"),
    @JsonSubTypes.Type(value = DomainTarget.ExternalCName.class, name = "external-cname"),
    @JsonSubTypes.Type(value = DomainTarget.Gateway.class, name = "gateway")
})
public sealed interface DomainTarget permits DomainTarget.IpAddress, DomainTarget.Tunnel, DomainTarget.ExternalCName, DomainTarget.Gateway {
    record IpAddress(DomainTargetIp details) implements DomainTarget {}
    record Tunnel(DomainTargetTunnel details) implements DomainTarget {}
    record ExternalCName(DomainTargetExternalCName details) implements DomainTarget {}
    record Gateway(DomainTargetGateway details) implements DomainTarget {}
}
