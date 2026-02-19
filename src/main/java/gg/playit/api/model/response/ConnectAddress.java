package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConnectAddress.Addr4.class, name = "addr4"),
    @JsonSubTypes.Type(value = ConnectAddress.Addr6.class, name = "addr6"),
    @JsonSubTypes.Type(value = ConnectAddress.Ip4.class, name = "ip4"),
    @JsonSubTypes.Type(value = ConnectAddress.Ip6.class, name = "ip6"),
    @JsonSubTypes.Type(value = ConnectAddress.Auto.class, name = "auto"),
    @JsonSubTypes.Type(value = ConnectAddress.Domain.class, name = "domain")
})
public sealed interface ConnectAddress permits ConnectAddress.Addr4, ConnectAddress.Addr6, ConnectAddress.Ip4, ConnectAddress.Ip6, ConnectAddress.Auto, ConnectAddress.Domain {
    record Addr4(ConnectAddr4 value) implements ConnectAddress {}
    record Addr6(ConnectAddr6 value) implements ConnectAddress {}
    record Ip4(ConnectIp4 value) implements ConnectAddress {}
    record Ip6(ConnectIp6 value) implements ConnectAddress {}
    record Auto(ConnectAutoName value) implements ConnectAddress {}
    record Domain(ConnectDomain value) implements ConnectAddress {}
}
