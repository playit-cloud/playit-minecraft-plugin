package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gg.playit.api.model.enums.AccountStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebAuthToken(
    int update_version,
    long account_id,
    long timestamp,
    AccountStatus account_status,
    TotpStatus totp_status,
    Long admin_id,
    Long admin_review_id,
    boolean read_only,
    boolean show_admin
) {}
