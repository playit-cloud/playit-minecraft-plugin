package gg.playit.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionStatus {
    @JsonProperty("account_id")
    public long accountId;

    @JsonProperty("is_guest")
    public boolean isGuest;

    @JsonProperty("email_verified")
    public boolean emailVerified;

    @JsonProperty("agent_id")
    public String agentId;

    public Notice notice;

    @Override
    public String toString() {
        return "SessionStatus{" +
                "accountId=" + accountId +
                ", isGuest=" + isGuest +
                ", emailVerified=" + emailVerified +
                ", agentId='" + agentId + '\'' +
                ", notice=" + notice +
                '}';
    }
}
