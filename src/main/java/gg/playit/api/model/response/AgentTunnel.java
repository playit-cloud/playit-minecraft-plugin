package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.AgentTunnelDisabled;
import gg.playit.api.model.enums.PortType;
import gg.playit.api.model.enums.ProxyProtocol;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentTunnel(
    String id,
    long internal_id,
    String name,
    long ip_num,
    int region_num,
    PortRange port,
    PortType proto,
    String local_ip,
    int local_port,
    String tunnel_type,
    String assigned_domain,
    String custom_domain,
    AgentTunnelDisabled disabled,
    ProxyProtocol proxy_protocol,
    AgentTunnelConfig agent_config
) {}
