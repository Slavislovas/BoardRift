package com.socialnetwork.boardrift.rest.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BGGThingResponse {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        @JacksonXmlProperty(isAttribute = true)
        private String type;

        @JacksonXmlProperty(isAttribute = true)
        private String id;

        @JacksonXmlProperty(localName = "thumbnail")
        private String thumbnail;

        @JacksonXmlProperty(localName = "image")
        private String image;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "name")
        private List<Name> names;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "yearpublished")
        private YearPublished yearPublished;

        @JacksonXmlProperty(localName = "minplayers")
        private MinPlayers minPlayers;

        @JacksonXmlProperty(localName = "maxplayers")
        private MaxPlayers maxPlayers;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Name {

            @JacksonXmlProperty(isAttribute = true)
            private String type;

            @JacksonXmlProperty(isAttribute = true)
            private int sortindex;

            @JacksonXmlProperty(isAttribute = true)
            private String value;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class YearPublished {

            @JacksonXmlProperty(isAttribute = true)
            private int value;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MinPlayers {

            @JacksonXmlProperty(isAttribute = true)
            private int value;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MaxPlayers {

            @JacksonXmlProperty(isAttribute = true)
            private int value;
        }
    }
}
