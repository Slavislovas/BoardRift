package com.socialnetwork.boardrift.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WarningDto {
    private Long id;
    private String reason;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date issuedDate;
}
