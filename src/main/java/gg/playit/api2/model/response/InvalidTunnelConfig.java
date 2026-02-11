package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvalidTunnelConfig(String agent_schema_id, AgentTunnelSchema current_schema, AgentTunnelSchema target_schema) {}
