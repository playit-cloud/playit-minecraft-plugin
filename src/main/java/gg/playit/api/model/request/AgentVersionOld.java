package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.Platform;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentVersionOld(@NotNull Platform platform, @NotNull String version, Boolean has_expired) {}
