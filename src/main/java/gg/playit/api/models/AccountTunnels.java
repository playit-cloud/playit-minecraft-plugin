package gg.playit.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTunnels {
    @JsonProperty
    public List<AccountTunnel> tunnels;

    @JsonProperty("agent_id")
    public String agentId;

    @Override
    public String toString() {
        return "AccountTunnels{" +
                "tunnels=" + tunnels +
                ", agentId='" + agentId + '\'' +
                '}';
    }
}
