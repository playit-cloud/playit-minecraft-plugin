package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsCreateV1(
    String name,
    TunnelProtocol protocol,
    AccountTunnelOriginCreate origin,
    CreateTunnelEndpoint endpoint,
    Boolean enabled,
    String firewall_id
) {}
