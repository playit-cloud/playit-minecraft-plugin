package gg.playit.api.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import gg.playit.api.models.PortType;
import gg.playit.api.models.TunnelType;

import java.net.InetAddress;

public class CreateTunnel implements Action {
    @JsonProperty("tunnel_type")
    public TunnelType tunnelType;

    @JsonProperty("port_type")
    public PortType portType;

    @JsonProperty("port_count")
    public int portCount;

    @JsonProperty("local_ip")
    public String localIp;

    @JsonProperty("local_port")
    public Integer localPort;

    @JsonProperty("agent_id")
    public String agentId;

    @Override
    public String getPath() {
        return "/account";
    }

    @Override
    public String getType() {
        return "create-tunnel";
    }
}
