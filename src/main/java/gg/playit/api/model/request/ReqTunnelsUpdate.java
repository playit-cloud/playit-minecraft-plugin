package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqTunnelsUpdate(
    String tunnel_id,
    String local_ip,
    Integer local_port,
    String agent_id,
    boolean enabled
) {}
