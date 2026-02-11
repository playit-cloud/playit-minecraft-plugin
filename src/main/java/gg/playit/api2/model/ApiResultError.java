package gg.playit.api2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiResultError<S, F>(ApiResponseError data) implements ApiResult<S, F> {}
