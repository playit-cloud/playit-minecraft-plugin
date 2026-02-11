package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentSchema(
    AgentTunnelSchema default_schema,
    List<AgentSchemaForTunnelType> schemas,
    Boolean only_explicit_schemas
) {}
