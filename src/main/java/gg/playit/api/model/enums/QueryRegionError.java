package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum QueryRegionError {
    FailedToDetermineLocation("FailedToDetermineLocation");

    private final String value;

    QueryRegionError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
