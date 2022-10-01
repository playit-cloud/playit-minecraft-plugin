package gg.playit.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebSession {
    public long accountId;
    public String sessionKey;
    public boolean isGuest;
    public boolean emailVerified;
}
