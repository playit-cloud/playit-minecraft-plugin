package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.AccountStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentPermissions(boolean is_self_managed, boolean has_premium, AccountStatus account_status) {}
