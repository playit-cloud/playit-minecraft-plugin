package gg.playit.api.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface Action {
    @JsonIgnore
    String getPath();

    @JsonProperty("type")
    String getType();
}
