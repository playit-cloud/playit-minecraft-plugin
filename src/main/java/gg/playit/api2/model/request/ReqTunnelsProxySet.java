package gg.playit.api2.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.ProxyProtocol;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsProxySet(String tunnel_id, ProxyProtocol proxy_protocol) {}
