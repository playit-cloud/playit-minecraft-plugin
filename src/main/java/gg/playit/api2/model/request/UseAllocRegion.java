package gg.playit.api2.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.PlayitNetwork;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UseAllocRegion(PlayitNetwork region, Integer port) {}
