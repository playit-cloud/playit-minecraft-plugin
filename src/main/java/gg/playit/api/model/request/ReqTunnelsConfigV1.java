package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.response.AgentTunnelConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsConfigV1(String tunnel_id, String new_agent_id, AgentTunnelConfig new_config) {}
