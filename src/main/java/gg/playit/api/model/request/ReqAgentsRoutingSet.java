package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqAgentsRoutingSet(@NotNull String agent_id, @NotNull AgentRoutingTarget routing, Boolean disable_ip6) {}
