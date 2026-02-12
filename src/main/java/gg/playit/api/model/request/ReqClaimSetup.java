package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.ClaimAgentType;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqClaimSetup(@NotNull String code, @NotNull ClaimAgentType agent_type, @NotNull String version) {}
