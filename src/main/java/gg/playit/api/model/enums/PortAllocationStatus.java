package gg.playit.api.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PortAllocationStatus {
    Pending("Pending"),
    RanOutOfPorts("RanOutOfPorts"),
    PublicPortNotAvailable("PublicPortNotAvailable"),
    NoPortsAvailableOnIp("NoPortsAvailableOnIp"),
    AccountPortLimitReached("AccountPortLimitReached");

    private final String value;

    PortAllocationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
