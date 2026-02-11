package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentRouting(
    String agent_id,
    List<String> targets4,
    List<String> targets6,
    boolean disable_ip6
) {}
