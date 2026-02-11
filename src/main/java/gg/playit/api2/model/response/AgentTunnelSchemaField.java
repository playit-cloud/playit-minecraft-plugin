package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.AgentTunnelAttrType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentTunnelSchemaField(
    String label,
    String description,
    AgentTunnelAttrType value_type,
    boolean allow_null,
    String default_value,
    List<String> variants
) {}
