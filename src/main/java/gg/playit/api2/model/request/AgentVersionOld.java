package gg.playit.api2.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.Platform;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentVersionOld(Platform platform, String version, Boolean has_expired) {}
