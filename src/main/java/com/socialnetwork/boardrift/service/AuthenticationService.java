package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.enumeration.VerificationTokenType;
import com.socialnetwork.boardrift.repository.VerificationTokenRepository;
import com.socialnetwork.boardrift.repository.UserRepository;
import com.socialnetwork.boardrift.repository.model.VerificationTokenEntity;
import com.socialnetwork.boardrift.repository.model.RefreshTokenEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.rest.model.AuthenticationRequestDto;
import com.socialnetwork.boardrift.rest.model.AuthenticationResponseDto;
import com.socialnetwork.boardrift.rest.model.PasswordResetProcessDto;
import com.socialnetwork.boardrift.rest.model.PasswordResetRequestDto;
import com.socialnetwork.boardrift.rest.model.RefreshTokenRequestDto;
import com.socialnetwork.boardrift.util.exception.EmailNotVerifiedException;
import com.socialnetwork.boardrift.util.exception.FieldValidationException;
import com.socialnetwork.boardrift.util.exception.InvalidLoginCredentialsException;
import com.socialnetwork.boardrift.util.exception.TokenRefreshException;
import com.socialnetwork.boardrift.util.exception.UnauthorizedException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${reset-password.expiry-time-in-minutes}")
    private Integer passwordResetTokenExpiryTimeInMinutes;

    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (DisabledException disabledException) {
            UserEntity suspendedUserEntity = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new EntityNotFoundException("User with email: " + request.getEmail()  + " was not found"));

            StringBuilder suspensionReasonStringBuilder = new StringBuilder();
            suspensionReasonStringBuilder.append("This user has been suspended for:\n");
            suspensionReasonStringBuilder.append(suspendedUserEntity.getSuspension().getReason());

            throw new UnauthorizedException(suspensionReasonStringBuilder.toString());
        } catch (Exception ex) {
            throw new InvalidLoginCredentialsException();
        }

        UserEntity userEntity = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new EntityNotFoundException("User with email: " + request.getEmail()  + " was not found"));

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

    public void logout(RefreshTokenRequestDto refreshTokenRequestDto) {
        jwtService.deleteRefreshTokenByToken(refreshTokenRequestDto.getToken());
    }

    public void createResetPasswordRequest(PasswordResetRequestDto passwordResetRequest) {
        UserEntity userEntity = userRepository.findByEmail(passwordResetRequest.getEmail())
                .orElse(null);

        if (userEntity == null) {
            return;
        }

        VerificationTokenEntity passwordResetTokenEntity = new VerificationTokenEntity(null, VerificationTokenType.PASSWORD_RESET, userEntity, passwordResetTokenExpiryTimeInMinutes);
        passwordResetTokenEntity = passwordResetTokenRepository.save(passwordResetTokenEntity);

        emailService.sendResetPasswordRequest(userEntity, passwordResetTokenEntity);
    }

    public void processResetPasswordRequest(PasswordResetProcessDto passwordResetProcessDto) {
        VerificationTokenEntity passwordResetTokenEntity = passwordResetTokenRepository
                .findByTokenAndType(passwordResetProcessDto.getToken(), VerificationTokenType.PASSWORD_RESET).orElseThrow(() -> new EntityNotFoundException("Password reset token was not found"));

        if (!passwordResetProcessDto.getNewPassword().equals(passwordResetProcessDto.getConfirmationPassword())) {
            throw new FieldValidationException(Map.of("newPassword", "Passwords do not match", "confirmationPassword", "Passwords do not match"));
        }

        UserEntity userEntity = passwordResetTokenEntity.getUser();
        userEntity.setPassword(bCryptPasswordEncoder.encode(passwordResetProcessDto.getNewPassword()));

        userRepository.save(userEntity);
        passwordResetTokenRepository.delete(passwordResetTokenEntity);
    }
}
