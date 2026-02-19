package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentTunnelSchema(Map<String, AgentTunnelSchemaField> fields) {}
