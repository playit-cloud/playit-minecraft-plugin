package gg.playit.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PortType {
    @JsonProperty("tcp")
    TCP,
    @JsonProperty("udp")
    UDP,
    @JsonProperty("both")
    BOTH;

    @JsonValue
    @Override
    public String toString() {
        if (this == TCP) {
            return "tcp";
        }
        if (this == UDP) {
            return "udp";
        }
        if (this == BOTH) {
            return "both";
        }
        throw new IllegalStateException();
    }
}