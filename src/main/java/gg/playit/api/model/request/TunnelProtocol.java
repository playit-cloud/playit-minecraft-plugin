package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gg.playit.api.model.enums.TunnelType;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TunnelProtocol.TunnelTypeDetail.class, name = "tunnel-type"),
    @JsonSubTypes.Type(value = TunnelProtocol.RawPorts.class, name = "raw-ports")
})
public sealed interface TunnelProtocol permits TunnelProtocol.TunnelTypeDetail, TunnelProtocol.RawPorts {
    record TunnelTypeDetail(@NotNull TunnelType details) implements TunnelProtocol {}
    record RawPorts(@NotNull TunnelProtocolRawPorts details) implements TunnelProtocol {}
}
