package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PropsetDetails.HostnameVerifyLevel.class, name = "hostname_verify_level"),
    @JsonSubTypes.Type(value = PropsetDetails.CustomTunnelDetails.class, name = "custom_tunnel_details")
})
public sealed interface PropsetDetails permits PropsetDetails.HostnameVerifyLevel, PropsetDetails.CustomTunnelDetails {
    record HostnameVerifyLevel(@NotNull HostnameVerifyLevel value) implements PropsetDetails {}
    record CustomTunnelDetails(@NotNull String value) implements PropsetDetails {}
}
