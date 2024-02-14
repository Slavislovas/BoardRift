package com.socialnetwork.boardrift.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BGGSearchResponse {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @JacksonXmlProperty(isAttribute = true)
        private Long id;

        @JsonProperty("name")
        private Name name;

        @JsonProperty("yearpublished")
        private YearPublished yearPublished;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearPublished {
        @JacksonXmlProperty(isAttribute = true)
        private Integer value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Name {
        @JacksonXmlProperty(isAttribute = true)
        private String value;
    }
}