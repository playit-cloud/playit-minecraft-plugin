package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PortType;
import gg.playit.api.model.enums.ProxyProtocol;
import gg.playit.api.model.enums.TunnelType;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsCreate(
    String name,
    TunnelType tunnel_type,
    String tunnel_description,
    @NotNull PortType port_type,
    int port_count,
    @NotNull TunnelOriginCreate origin,
    boolean enabled,
    TunnelCreateUseAllocation alloc,
    String firewall_id,
    ProxyProtocol proxy_protocol
) {}
