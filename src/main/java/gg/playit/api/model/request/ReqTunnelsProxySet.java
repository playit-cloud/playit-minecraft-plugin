package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.ProxyProtocol;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsProxySet(@NotNull String tunnel_id, ProxyProtocol proxy_protocol) {}
