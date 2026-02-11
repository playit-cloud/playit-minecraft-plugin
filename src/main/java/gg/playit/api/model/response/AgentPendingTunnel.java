package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PortType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentPendingTunnel(
    String id,
    String name,
    PortType proto,
    int port_count,
    String tunnel_type,
    boolean is_disabled,
    int region_num
) {}
