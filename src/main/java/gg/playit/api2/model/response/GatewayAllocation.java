package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.PlayitNetwork;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GatewayAllocation(String id, String hostname, PlayitNetwork region) {}
