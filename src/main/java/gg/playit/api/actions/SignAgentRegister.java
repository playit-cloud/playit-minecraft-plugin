package gg.playit.api.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import gg.playit.messages.SocketAddr;

import java.net.InetSocketAddress;

public class SignAgentRegister implements Action {
    @JsonProperty("agent_version")
    public int agentVersion;
    @JsonProperty("client_addr")
    @JsonSerialize(using = ToStringSerializer.class)
    public SocketAddr clientAddr;
    @JsonProperty("tunnel_addr")
    @JsonSerialize(using = ToStringSerializer.class)
    public SocketAddr tunnelAddr;

    @Override
    public String getPath() {
        return "/agent";
    }

    @Override
    public String getType() {
        return "sign-agent-register";
    }
}
