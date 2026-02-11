package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsRename(String tunnel_id, String name) {}
