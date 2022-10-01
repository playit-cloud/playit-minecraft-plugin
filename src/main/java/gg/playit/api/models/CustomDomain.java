package gg.playit.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomDomain {
    public String id;
    public String name;

    @Override
    public String toString() {
        return "CustomDomain{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
