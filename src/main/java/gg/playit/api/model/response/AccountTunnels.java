package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountTunnels(
    List<AccountTunnel> tunnels,
    AllocatedPorts tcp_alloc,
    AllocatedPorts udp_alloc
) {}
