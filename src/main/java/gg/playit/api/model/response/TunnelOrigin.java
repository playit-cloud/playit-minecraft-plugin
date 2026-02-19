package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TunnelOrigin.Agent.class, name = "agent"),
    @JsonSubTypes.Type(value = TunnelOrigin.Managed.class, name = "managed")
})
public sealed interface TunnelOrigin permits TunnelOrigin.Agent, TunnelOrigin.Managed {
    record Agent(AssignedAgent data) implements TunnelOrigin {}
    record Managed(AssignedManaged data) implements TunnelOrigin {}
}
