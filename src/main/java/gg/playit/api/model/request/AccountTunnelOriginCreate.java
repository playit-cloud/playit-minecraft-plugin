package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AccountTunnelOriginCreate.Agent.class, name = "agent")
})
public sealed interface AccountTunnelOriginCreate permits AccountTunnelOriginCreate.Agent {
    record Agent(AgentOrigin data) implements AccountTunnelOriginCreate {}
}
