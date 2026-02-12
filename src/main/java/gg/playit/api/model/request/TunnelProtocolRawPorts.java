package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PortType;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TunnelProtocolRawPorts(@NotNull PortType port_type, int port_count, @NotNull String software_description) {}
