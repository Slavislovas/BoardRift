package com.socialnetwork.boardrift.rest.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
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
    @JacksonXmlProperty(localName = "boardgame")
    private List<BoardGame> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoardGame {

        @JacksonXmlProperty(isAttribute = true)
        private String type;

        @JacksonXmlProperty(isAttribute = true)
        private String id;

        @JacksonXmlProperty(localName = "thumbnail")
        private String thumbnail;

        @JacksonXmlProperty(localName = "image")
        private String image;

        @JacksonXmlProperty(localName = "boardgamecategory")
        private String boardGameCategory;

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

            @JacksonXmlText
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
