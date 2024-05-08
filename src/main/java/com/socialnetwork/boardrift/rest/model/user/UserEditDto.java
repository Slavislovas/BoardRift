package com.socialnetwork.boardrift.rest.model.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Valid
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEditDto {
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z ]+$",
            message = "Name can only contain alphabetic characters and spaces")
    @Size(min = 1, max = 25, message = "A maximum of 25 characters is allowed for name")
    private String name;

    @NotBlank(message = "Lastname is required")
    @Pattern(regexp = "^[a-zA-Z ]+$",
            message = "Lastname can only contain alphabetic characters and spaces")
    @Size(min = 1, max = 25, message = "A maximum of 25 characters is allowed for lastname")
    private String lastname;

    @Size(min = 0, max = 500, message = "Bio must be between 0 and 500 characters")
    private String bio;

    @NotNull(message = "Public posts privacy setting required")
    private Boolean publicPosts;

    @NotNull(message = "Public friends list privacy setting required")
    private Boolean publicFriendsList;

    @NotNull(message = "Public plays privacy setting required")
    private Boolean publicPlays;

    @NotNull(message = "Public statistics privacy setting required")
    private Boolean publicStatistics;

    @Size(min = 1, max = 50)
    private String country;

    @Size(min = 1, max = 50)
    private String city;
}
