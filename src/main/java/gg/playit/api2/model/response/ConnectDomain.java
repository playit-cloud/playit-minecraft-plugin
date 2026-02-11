package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.DomainMode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConnectDomain(String id, String domain, String address, DomainMode mode, ConnectAddressSource source) {}
