package gg.playit.api.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthError {
    AuthRequired("AuthRequired"),
    InvalidHeader("InvalidHeader"),
    InvalidSignature("InvalidSignature"),
    InvalidTimestamp("InvalidTimestamp"),
    InvalidApiKey("InvalidApiKey"),
    InvalidAgentKey("InvalidAgentKey"),
    SessionExpired("SessionExpired"),
    InvalidAuthType("InvalidAuthType"),
    ScopeNotAllowed("ScopeNotAllowed"),
    NoLongerValid("NoLongerValid"),
    GuestAccountNotAllowed("GuestAccountNotAllowed"),
    EmailMustBeVerified("EmailMustBeVerified"),
    AccountDoesNotExist("AccountDoesNotExist"),
    AdminOnly("AdminOnly"),
    InvalidToken("InvalidToken"),
    TotpRequred("TotpRequred"),
    NotAllowedWithReadOnly("NotAllowedWithReadOnly"),
    DefaultAgentBlocked("DefaultAgentBlocked"),
    AgentNotSelfManaged("AgentNotSelfManaged"),
    SelfManagedAgentCanOnlyAffectSelf("SelfManagedAgentCanOnlyAffectSelf"),
    AccountNotAuthorized("AccountNotAuthorized");

    private final String value;

    AuthError(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
