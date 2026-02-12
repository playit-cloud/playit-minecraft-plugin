package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsCreateV1(
    @NotNull String name,
    @NotNull TunnelProtocol protocol,
    @NotNull AccountTunnelOriginCreate origin,
    @NotNull CreateTunnelEndpoint endpoint,
    Boolean enabled,
    String firewall_id
) {}
