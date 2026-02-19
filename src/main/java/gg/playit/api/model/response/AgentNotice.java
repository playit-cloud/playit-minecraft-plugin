package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.AgentNoticePriority;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentNotice(AgentNoticePriority priority, String message, String resolve_link) {}
