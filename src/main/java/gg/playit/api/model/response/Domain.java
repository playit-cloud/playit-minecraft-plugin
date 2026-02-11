package gg.playit.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Domain(
    String id,
    String name,
    boolean is_external,
    String parent,
    String sub_id,
    DomainTarget target
) {}
