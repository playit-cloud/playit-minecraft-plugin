package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.PlayitNetwork;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortAllocationRequest(
    String id,
    String status,
    PlayitNetwork region,
    Integer public_port,
    String public_ip
) {}
