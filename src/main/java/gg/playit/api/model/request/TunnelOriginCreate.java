package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TunnelOriginCreate.Default.class, name = "default"),
    @JsonSubTypes.Type(value = TunnelOriginCreate.Agent.class, name = "agent"),
    @JsonSubTypes.Type(value = TunnelOriginCreate.Managed.class, name = "managed")
})
public sealed interface TunnelOriginCreate permits TunnelOriginCreate.Default, TunnelOriginCreate.Agent, TunnelOriginCreate.Managed {
    record Default(@NotNull AssignedDefaultCreate data) implements TunnelOriginCreate {}
    record Agent(@NotNull AssignedAgentCreate data) implements TunnelOriginCreate {}
    record Managed(@NotNull AssignedManagedCreate data) implements TunnelOriginCreate {}
}
