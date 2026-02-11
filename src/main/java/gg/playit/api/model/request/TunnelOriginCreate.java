package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TunnelOriginCreate.Default.class, name = "default"),
    @JsonSubTypes.Type(value = TunnelOriginCreate.Agent.class, name = "agent"),
    @JsonSubTypes.Type(value = TunnelOriginCreate.Managed.class, name = "managed")
})
public sealed interface TunnelOriginCreate permits TunnelOriginCreate.Default, TunnelOriginCreate.Agent, TunnelOriginCreate.Managed {
    record Default(AssignedDefaultCreate data) implements TunnelOriginCreate {}
    record Agent(AssignedAgentCreate data) implements TunnelOriginCreate {}
    record Managed(AssignedManagedCreate data) implements TunnelOriginCreate {}
}
