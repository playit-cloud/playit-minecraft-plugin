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
        return switch (this) {
            case TCP -> "tcp";
            case UDP -> "udp";
            case BOTH -> "both";
        };
    }
}