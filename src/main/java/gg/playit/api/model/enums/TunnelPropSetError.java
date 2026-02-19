package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelPropSetError {
    RequiresPermium("RequiresPermium"),
    TunnelNotFound("TunnelNotFound"),
    PropertyValueNotSupportedForTunnelType("PropertyValueNotSupportedForTunnelType"),
    PropertyValueInvalid("PropertyValueInvalid");

    private final String value;

    TunnelPropSetError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
