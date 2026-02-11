package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PlayitNetwork;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UseRegion(PlayitNetwork region) {}
