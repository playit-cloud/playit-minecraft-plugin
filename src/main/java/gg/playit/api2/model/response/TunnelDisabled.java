package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.TunnelOfflineReason;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TunnelDisabled(TunnelOfflineReason reason) {}
