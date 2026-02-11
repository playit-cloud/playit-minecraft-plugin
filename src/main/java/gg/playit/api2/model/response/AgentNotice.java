package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.AgentNoticePriority;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentNotice(AgentNoticePriority priority, String message, String resolve_link) {}
