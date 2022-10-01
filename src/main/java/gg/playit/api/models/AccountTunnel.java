package gg.playit.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetAddress;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTunnel {
    @JsonProperty
    public String id;

    @JsonProperty
    public boolean enabled;

    @JsonProperty
    public String name;

    @JsonProperty("ip_address")
    public InetAddress ipAddress;

    @JsonProperty("ip_hostname")
    public String ipHostname;

    @JsonProperty("custom_domain")
    public CustomDomain customDomain;

    @JsonProperty("assigned_domain")
    public String assignedDomain;

    @JsonProperty("display_address")
    public String displayAddress;

    @JsonProperty("is_dedicated_ip")
    public boolean isDedicatedIp;

    @JsonProperty("from_port")
    public int fromPort;

    @JsonProperty("to_port")
    public int toPort;

    @JsonProperty("tunnel_type")
    public TunnelType tunnelType;

    @JsonProperty("port_type")
    public PortType portType;

    @JsonProperty("firewall_id")
    public String firewallId;

    @Override
    public String toString() {
        return "AccountTunnel{" +
                "id='" + id + '\'' +
                ", enabled=" + enabled +
                ", name='" + name + '\'' +
                ", ipAddress=" + ipAddress +
                ", ipHostname='" + ipHostname + '\'' +
                ", customDomain=" + customDomain +
                ", assignedDomain='" + assignedDomain + '\'' +
                ", displayAddress='" + displayAddress + '\'' +
                ", isDedicatedIp=" + isDedicatedIp +
                ", fromPort=" + fromPort +
                ", toPort=" + toPort +
                ", tunnelType=" + tunnelType +
                ", portType=" + portType +
                ", firewallId='" + firewallId + '\'' +
                '}';
    }
}
