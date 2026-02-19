package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.IpType;
import gg.playit.api.model.enums.PlayitNetwork;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TunnelAllocated(
    String id,
    String ip_hostname,
    String static_ip4,
    String static_ip6,
    String assigned_domain,
    String assigned_srv,
    String tunnel_ip,
    int port_start,
    int port_end,
    TunnelAssignment assignment,
    IpType ip_type,
    PlayitNetwork region
) {}
