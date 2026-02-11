package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.HostnameVerifyLevel;
import gg.playit.api2.model.enums.PlayitNetwork;
import gg.playit.api2.model.enums.PortType;
import gg.playit.api2.model.enums.ProxyProtocol;
import gg.playit.api2.model.enums.TunnelOfflineReason;
import gg.playit.api2.model.enums.TunnelType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountTunnel(
    String id,
    TunnelType tunnel_type,
    String created_at,
    String name,
    PortType port_type,
    int port_count,
    AccountTunnelAllocation alloc,
    TunnelOrigin origin,
    TunnelDomain domain,
    String firewall_id,
    Ratelimit ratelimit,
    boolean active,
    TunnelOfflineReason disabled_reason,
    PlayitNetwork region,
    ExpireNotice expire_notice,
    ProxyProtocol proxy_protocol,
    HostnameVerifyLevel hostname_verify_level,
    boolean agent_over_limit
) {}
