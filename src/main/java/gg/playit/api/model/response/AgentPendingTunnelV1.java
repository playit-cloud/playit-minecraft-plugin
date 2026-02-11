package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PortType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentPendingTunnelV1(
    String id,
    String name,
    String tunnel_type,
    String tunnel_type_display,
    PortType port_type,
    int port_count,
    String status_msg
) {}
