package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentSchemaForTunnelType(AgentSchemaTunnelType tunnel_type, AgentTunnelSchema schema) {}
