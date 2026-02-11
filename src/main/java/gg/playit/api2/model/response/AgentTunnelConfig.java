package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentTunnelConfig(List<AgentTunnelAttr> fields) {
    public AgentTunnelConfig() {
        this(null);
    }
}
