package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.RefreshTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.rest.model.AuthenticationRequestDto;
import com.socialnetwork.boardrift.rest.model.AuthenticationResponseDto;
import com.socialnetwork.boardrift.rest.model.RefreshTokenRequestDto;
import com.socialnetwork.boardrift.util.exception.EmailNotVerifiedException;
import com.socialnetwork.boardrift.util.exception.InvalidLoginCredentialsException;
import com.socialnetwork.boardrift.util.exception.TokenRefreshException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception ex) {
            throw new InvalidLoginCredentialsException();
        }

        UserEntity userEntity = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new EntityNotFoundException("User with username: " + request.getUsername()  + " was not found"));

        if (!userEntity.getEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }

        if (jwtService.checkIfRefreshTokenExistsByUserId(userEntity.getId())) {
            jwtService.deleteRefreshTokenByUserId(userEntity.getId());
        }

        String refreshToken = jwtService.saveRefreshToken(userEntity).getToken();
        String jwtToken = jwtService.generateToken(userEntity);

        return new AuthenticationResponseDto(jwtToken, refreshToken);
    }

    public AuthenticationResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        RefreshTokenEntity refreshTokenEntity = jwtService.findRefreshTokenEntityByToken(refreshTokenRequestDto.getToken());
        if (refreshTokenEntity.getExpirationDate().before(new Date())) {
            jwtService.deleteRefreshTokenByUserId(refreshTokenEntity.getUser().getId());
            throw new TokenRefreshException("Your session has ended, please log in again");
        }

        String jwtToken = jwtService.generateToken(refreshTokenEntity.getUser());

        return new AuthenticationResponseDto(jwtToken, refreshTokenEntity.getToken());
    }
}
