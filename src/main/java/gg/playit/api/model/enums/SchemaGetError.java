package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SchemaGetError {
    SchemaNotFound("SchemaNotFound");

    private final String value;

    SchemaGetError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
