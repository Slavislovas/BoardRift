package com.socialnetwork.boardrift.rest.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Valid
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefreshTokenRequestDto {
    @NotBlank
    private String token;
}
