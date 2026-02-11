package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.DisabledReason;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExpireNotice(String disable_at, String remove_at, DisabledReason reason) {}
