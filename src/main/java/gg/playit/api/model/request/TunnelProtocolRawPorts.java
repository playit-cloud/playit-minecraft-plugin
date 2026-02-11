package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PortType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TunnelProtocolRawPorts(PortType port_type, int port_count, String software_description) {}
