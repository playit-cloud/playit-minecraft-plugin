package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PortType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentTunnelV1(
    String id,
    long internal_id,
    String name,
    String display_address,
    PortType port_type,
    int port_count,
    String tunnel_type,
    String tunnel_type_display,
    AgentTunnelConfig agent_config,
    String disabled_reason
) {}
