package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.Platform;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReqProtoRegister(
    PlayitAgentVersion agent_version,
    Long proto_version,
    AgentVersion version,
    Platform platform,
    @NotNull String client_addr,
    @NotNull String tunnel_addr
) {}
