package com.socialnetwork.boardrift.rest.controller;

import com.socialnetwork.boardrift.rest.model.AuthenticationRequestDto;
import com.socialnetwork.boardrift.rest.model.AuthenticationResponseDto;
import com.socialnetwork.boardrift.rest.model.RefreshTokenRequestDto;
import com.socialnetwork.boardrift.service.AuthenticationService;
import com.socialnetwork.boardrift.util.RequestValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> authenticate(@Valid @RequestBody AuthenticationRequestDto authenticationRequestDto, BindingResult bindingResult) {
        RequestValidator.validateRequest(bindingResult);
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequestDto));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("success");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto, BindingResult bindingResult) throws IOException {
        RequestValidator.validateRequest(bindingResult);
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequestDto));
    }
}