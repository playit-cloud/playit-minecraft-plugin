package gg.playit.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Notice {
    @JsonProperty
    public String url;

    @JsonProperty
    public String message;

    @Override
    public String toString() {
        return "Notice{" +
                "url='" + url + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
