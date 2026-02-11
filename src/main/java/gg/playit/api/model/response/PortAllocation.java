package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.PlayitNetwork;
import gg.playit.api.model.enums.PortType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortAllocation(
    String alloc_id,
    PlayitNetwork ip_region,
    String ip_hostname,
    String auto_domain,
    String ip,
    int port,
    int port_count,
    PortType port_type,
    ExpireNotice expire_notice
) {}
