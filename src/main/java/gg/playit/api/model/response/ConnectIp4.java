package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConnectIp4(String address, int default_port, ConnectAddressSource source) {}
