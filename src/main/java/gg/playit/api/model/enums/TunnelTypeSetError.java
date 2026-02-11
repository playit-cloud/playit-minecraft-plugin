package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TunnelTypeSetError {
    RequiresPermium("RequiresPermium"),
    TunnelNotFound("TunnelNotFound"),
    TunnelHasInvalidSettingsForType("TunnelHasInvalidSettingsForType"),
    CannotChangeTunnelType("CannotChangeTunnelType");

    private final String value;

    TunnelTypeSetError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
