package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssignedAgentCreate(@NotNull String agent_id, @NotNull String local_ip, Integer local_port) {}
