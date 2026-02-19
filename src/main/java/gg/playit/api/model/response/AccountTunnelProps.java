package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.HostnameVerifyLevel;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountTunnelProps(HostnameVerifyLevel hostname_verify_level) {}
