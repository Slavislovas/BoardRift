package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.UserRegistrationDto;
import com.socialnetwork.boardrift.rest.model.UserRetrievalDto;
import com.socialnetwork.boardrift.service.UserService;
import com.socialnetwork.boardrift.util.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RequiredArgsConstructor
@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    @GetMapping("/register/confirm")
    public ResponseEntity<Void> confirmUserRegistration(@RequestParam("token") String token) {
        userService.confirmUserRegistration(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserRetrievalDto> createUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto, BindingResult bindingResult, HttpServletRequest servletRequest) {
        RequestValidator.validateRequest(bindingResult);
        return new ResponseEntity<>(userService.createUser(userRegistrationDto, servletRequest), HttpStatus.CREATED);
    }
}
