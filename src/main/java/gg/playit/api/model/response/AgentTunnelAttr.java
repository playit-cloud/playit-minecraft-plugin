package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentTunnelAttr(@NotNull String name, @NotNull String value) {}
