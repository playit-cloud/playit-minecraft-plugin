package gg.playit.api2.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsRatelimit(String tunnel_id, Integer bytes_per_second, Integer packets_per_second) {}
