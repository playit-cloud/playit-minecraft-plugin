package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentRunDataV1(
    String agent_id,
    List<AgentTunnelV1> tunnels,
    List<AgentPendingTunnelV1> pending,
    List<AgentNotice> notices,
    AgentPermissions permissions
) {}
