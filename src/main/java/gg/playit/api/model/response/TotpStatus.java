package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "status")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TotpStatus.Required.class, name = "required"),
    @JsonSubTypes.Type(value = TotpStatus.NotSetup.class, name = "not-setup"),
    @JsonSubTypes.Type(value = TotpStatus.Signed.class, name = "signed")
})
public sealed interface TotpStatus permits TotpStatus.Required, TotpStatus.NotSetup, TotpStatus.Signed {
    record Required() implements TotpStatus {}
    record NotSetup() implements TotpStatus {}
    record Signed(int epoch_sec) implements TotpStatus {}
}
