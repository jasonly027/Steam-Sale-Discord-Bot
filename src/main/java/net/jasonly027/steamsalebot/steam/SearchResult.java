package net.jasonly027.steamsalebot.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {
    @JsonProperty(value = "appid")
    private long appId;

    @JsonProperty(value = "name")
    private String name;

    public long getAppId() {
        return appId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + appId + ")";
    }
}
