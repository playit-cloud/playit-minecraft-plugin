package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.response.AgentTunnelConfig;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentOrigin(String agent_id, AgentTunnelConfig config) {}
