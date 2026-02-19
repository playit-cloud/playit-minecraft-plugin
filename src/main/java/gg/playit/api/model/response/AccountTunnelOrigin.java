package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AccountTunnelOrigin.NotSet.class, name = "not-set"),
    @JsonSubTypes.Type(value = AccountTunnelOrigin.Agent.class, name = "agent")
})
public sealed interface AccountTunnelOrigin permits AccountTunnelOrigin.NotSet, AccountTunnelOrigin.Agent {
    record NotSet(TunnelOriginNotSet details) implements AccountTunnelOrigin {}
    record Agent(TunnelToAgent details) implements AccountTunnelOrigin {}
}
