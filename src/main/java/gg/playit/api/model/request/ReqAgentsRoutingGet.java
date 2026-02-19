package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqAgentsRoutingGet(String agent_id) {}
