package gg.playit.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "status")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ApiSuccessNoFail.class, name = "success"),
    @JsonSubTypes.Type(value = ApiErrorNoFail.class, name = "error")
})
public sealed interface ApiResultNoFail<S> permits ApiSuccessNoFail, ApiErrorNoFail {
}

