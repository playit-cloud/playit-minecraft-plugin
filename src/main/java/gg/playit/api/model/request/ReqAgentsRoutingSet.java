package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqAgentsRoutingSet(String agent_id, AgentRoutingTarget routing, Boolean disable_ip6) {}
