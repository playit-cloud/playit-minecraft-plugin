package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AccountTunnelOriginCreate.Agent.class, name = "agent")
})
public sealed interface AccountTunnelOriginCreate permits AccountTunnelOriginCreate.Agent {
    record Agent(@NotNull AgentOrigin data) implements AccountTunnelOriginCreate {}
}
