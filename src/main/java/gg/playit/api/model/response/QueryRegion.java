package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PlayitNetwork;
import gg.playit.api.model.enums.PlayitPop;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QueryRegion(PlayitNetwork region, PlayitPop pop) {}
