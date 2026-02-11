package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HasAgentConfig(String config_schema_id, AgentTunnelConfig config_data) {}
