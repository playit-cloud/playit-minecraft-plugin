package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.ClaimAgentType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqClaimSetup(String code, ClaimAgentType agent_type, String version) {}
