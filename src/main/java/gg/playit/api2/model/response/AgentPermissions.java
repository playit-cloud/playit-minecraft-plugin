package gg.playit.api2.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api2.model.enums.AccountStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentPermissions(boolean is_self_managed, boolean has_premium, AccountStatus account_status) {}
