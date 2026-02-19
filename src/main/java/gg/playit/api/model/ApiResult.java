package gg.playit.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "status")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ApiSuccess.class, name = "success"),
    @JsonSubTypes.Type(value = ApiFail.class, name = "fail"),
    @JsonSubTypes.Type(value = ApiResultError.class, name = "error")
})
public sealed interface ApiResult<S, F> permits ApiSuccess, ApiFail, ApiResultError {
}
