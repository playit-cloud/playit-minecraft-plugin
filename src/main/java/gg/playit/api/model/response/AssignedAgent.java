package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssignedAgent(String agent_id, String agent_name, String local_ip, Integer local_port) {}
