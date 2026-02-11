package gg.playit.api2.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.Platform;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqProtoRegister(
    PlayitAgentVersion agent_version,
    Long proto_version,
    AgentVersion version,
    Platform platform,
    String client_addr,
    String tunnel_addr
) {}
