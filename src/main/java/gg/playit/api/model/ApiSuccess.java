package gg.playit.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiSuccess<S, F>(S data) implements ApiResult<S, F> {}
