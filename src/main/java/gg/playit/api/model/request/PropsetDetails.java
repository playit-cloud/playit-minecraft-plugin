package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gg.playit.api.model.enums.HostnameVerifyLevel;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PropsetDetails.HostnameVerifyLevel.class, name = "hostname_verify_level"),
    @JsonSubTypes.Type(value = PropsetDetails.CustomTunnelDetails.class, name = "custom_tunnel_details")
})
public sealed interface PropsetDetails permits PropsetDetails.HostnameVerifyLevel, PropsetDetails.CustomTunnelDetails {
    record HostnameVerifyLevel(HostnameVerifyLevel value) implements PropsetDetails {}
    record CustomTunnelDetails(String value) implements PropsetDetails {}
}
