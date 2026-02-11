package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UseAllocSharedIp(String ip_hostname, Integer port) {}
