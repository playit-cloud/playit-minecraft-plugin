package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.HostnameVerifyLevel;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountTunnelProps(HostnameVerifyLevel hostname_verify_level) {}
