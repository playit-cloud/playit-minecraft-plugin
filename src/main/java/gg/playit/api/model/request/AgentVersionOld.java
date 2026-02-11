package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.Platform;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentVersionOld(Platform platform, String version, Boolean has_expired) {}
