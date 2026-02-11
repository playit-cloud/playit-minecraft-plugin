package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.PlayitNetwork;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TunnelDedicatedIp(String sub_id, PlayitNetwork region) {}
