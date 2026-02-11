package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.TunnelOfflineReason;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TunnelDisabled(TunnelOfflineReason reason) {}
