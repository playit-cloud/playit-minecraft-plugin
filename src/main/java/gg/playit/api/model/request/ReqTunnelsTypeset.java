package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.TunnelType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsTypeset(String tunnel_id, TunnelType tunnel_type) {}
