package gg.playit.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ApiResponseError.Validation.class, name = "validation"),
    @JsonSubTypes.Type(value = ApiResponseError.PathNotFound.class, name = "path-not-found"),
    @JsonSubTypes.Type(value = ApiResponseError.Auth.class, name = "auth"),
    @JsonSubTypes.Type(value = ApiResponseError.Internal.class, name = "internal")
})
public sealed interface ApiResponseError permits ApiResponseError.Validation, ApiResponseError.PathNotFound, ApiResponseError.Auth, ApiResponseError.Internal {
    record Validation(String message) implements ApiResponseError {}
    record PathNotFound(PathNotFoundDetails message) implements ApiResponseError {}
    record Auth(AuthError message) implements ApiResponseError {}
    record Internal(ApiInternalError message) implements ApiResponseError {}

    record PathNotFoundDetails(String path) {}
    record ApiInternalError(String trace_id) {}
}
