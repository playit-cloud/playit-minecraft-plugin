package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.AccountTunnelOfflineReason;
import gg.playit.api.model.enums.PortType;
import gg.playit.api.model.enums.TunnelType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountTunnelV1(
    String id,
    String created_at,
    String name,
    boolean user_enabled,
    List<AccountTunnelOfflineReason> offline_reasons,
    TunnelType tunnel_type,
    PortType port_type,
    int port_count,
    String firewall_id,
    AccountTunnelProps props,
    AccountTunnelOrigin origin,
    List<PortAllocationRequest> port_allocation_requests,
    List<PublicAllocation> public_allocations,
    List<ConnectAddress> connect_addresses
) {}
