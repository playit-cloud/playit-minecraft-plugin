package gg.playit.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gg.playit.api.model.enums.PlayitNetwork;
import gg.playit.api.model.enums.PlayitPop;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AgentRoutingTarget.Automatic.class, name = "Automatic"),
    @JsonSubTypes.Type(value = AgentRoutingTarget.Pop.class, name = "Pop"),
    @JsonSubTypes.Type(value = AgentRoutingTarget.Region.class, name = "Region")
})
public sealed interface AgentRoutingTarget permits AgentRoutingTarget.Automatic, AgentRoutingTarget.Pop, AgentRoutingTarget.Region {
    record Automatic(Object details) implements AgentRoutingTarget {}
    record Pop(PlayitPop details) implements AgentRoutingTarget {}
    record Region(PlayitNetwork details) implements AgentRoutingTarget {}
}
