package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PlayitNetwork;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayitPops(List<Pop> pops, List<PlayitNetwork> regions) {}
