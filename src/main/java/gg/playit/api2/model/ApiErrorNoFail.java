package gg.playit.api2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiErrorNoFail<S>(ApiResponseError data) implements ApiResultNoFail<S> {}
