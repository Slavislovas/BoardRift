package com.socialnetwork.boardrift.rest.model.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.socialnetwork.boardrift.util.validation.AgeConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Valid
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z ]+$",
            message = "Name can only contain alphabetic characters and spaces")
    private String name;

    @NotBlank(message = "Lastname is required")
    @Pattern(regexp = "^[a-zA-Z ]+$",
            message = "Lastname can only contain alphabetic characters and spaces")
    private String lastname;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "Date of birth required")
    @Pattern(regexp = "^(?:19|20)\\d\\d-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[01])$",
            message = "Invalid date of birth. Please use the yyyy-MM-dd format.")
    @AgeConstraint(message = "You must be at least 18 years old")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dateOfBirth;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, contain at least one uppercase letter, lowercase letter, digit, special character (@$!%*?&)")
    private String password;
}
