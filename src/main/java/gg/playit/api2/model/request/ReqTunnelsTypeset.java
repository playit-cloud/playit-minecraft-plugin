package gg.playit.api2.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.TunnelType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsTypeset(String tunnel_id, TunnelType tunnel_type) {}
