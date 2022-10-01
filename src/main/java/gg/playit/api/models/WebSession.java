package gg.playit.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebSession {
    @JsonProperty("account_id")
    public long accountId;

    @JsonProperty("session_key")
    public String sessionKey;

    @JsonProperty("is_guest")
    public boolean isGuest;

    @JsonProperty("email_verified")
    public boolean emailVerified;
}
