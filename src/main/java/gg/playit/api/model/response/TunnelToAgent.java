package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TunnelToAgent(
    String agent_id,
    String name,
    String config_schema_id,
    AgentTunnelConfig config_data,
    InvalidTunnelConfig config_invalid
) {}
