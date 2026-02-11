package gg.playit.api2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFail<S, F>(F data) implements ApiResult<S, F> {}
