package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.AgentAccountStatus;
import gg.playit.api.model.enums.AgentType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentRunData(
    String agent_id,
    AgentType agent_type,
    AgentAccountStatus account_status,
    List<AgentTunnel> tunnels,
    List<AgentPendingTunnel> pending,
    AccountFeatures account_features
) {}
