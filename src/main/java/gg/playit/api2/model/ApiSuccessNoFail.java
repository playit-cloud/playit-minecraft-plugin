package gg.playit.api2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiSuccessNoFail<S>(S data) implements ApiResultNoFail<S> {}
