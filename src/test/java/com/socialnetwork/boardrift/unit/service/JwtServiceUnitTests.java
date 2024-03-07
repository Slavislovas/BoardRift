package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.RefreshTokenRepository;
import com.socialnetwork.boardrift.repository.model.RefreshTokenEntity;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.service.JwtService;
import com.socialnetwork.boardrift.util.exception.RefreshTokenNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith({MockitoExtension.class})
public class JwtServiceUnitTests {
    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    JwtService jwtService;

    UserEntity userEntity;

    Key signingKey;
    RefreshTokenEntity refreshTokenEntity;

    @BeforeEach
    void init()  {
        jwtService.setJwtSecretKey("GFR4d2dfg7gESRGZX52dxcbvrDF85qjGGFDS42121BVCXRDFUKJCVBDFGsd2");
        jwtService.setJwtAccessTokenLifetimeInMilliseconds(300000);
        jwtService.setJwtRefreshTokenLifetimeInMilliseconds(259200000);
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Username", "Password@123", "", "", "", true, false, false, "",
                Role.ROLE_USER, UserStatus.OFFLINE, false, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_LIST, Collections.EMPTY_SET, Collections.EMPTY_SET);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("GFR4d2dfg7gESRGZX52dxcbvrDF85qjGGFDS42121BVCXRDFUKJCVBDFGsd2"));
        refreshTokenEntity = new RefreshTokenEntity(null, new Date(System.currentTimeMillis() + 80000), userEntity);
    }

    @Test
    void generateTokenShouldSucceed() {
        String token = jwtService.generateToken(userEntity);

        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(signingKey)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
        Assertions.assertEquals(userEntity.getRole().name(), claims.get("role"));
        Assertions.assertEquals(userEntity.getId().toString(), claims.getSubject());
    }

    @Test
    void extractClaimShouldSucceedWhenValidJwtToken() {
        String token = Jwts
                .builder()
                .setSubject(userEntity.getUsername())
                .claim("role", userEntity.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 500000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        String result = jwtService.extractClaim(token, Claims::getSubject);

        Assertions.assertEquals(userEntity.getUsername(), result);
    }

    @Test
    void extractClaimShouldFailWhenInvalidJwtToken() {
        String token = Jwts
                .builder()
                .setSubject(userEntity.getUsername())
                .claim("role", userEntity.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() - 500000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Assertions.assertThrows(Exception.class, () -> jwtService.extractClaim(token, Claims::getSubject));
    }

    @Test
    void extractUsernameShouldSucceedWhenValidJwt() {
        String token = Jwts
                .builder()
                .setSubject(userEntity.getUsername())
                .claim("role", userEntity.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 500000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        String username = jwtService.extractId(token);

        Assertions.assertEquals(userEntity.getUsername(), username);
    }

    @Test
    void extractUsernameShouldFailWhenInvalidToken() {
        String token = Jwts
                .builder()
                .setSubject(userEntity.getUsername())
                .claim("role", userEntity.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() - 500000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Assertions.assertThrows(Exception.class, () -> jwtService.extractId(token));
    }

    @Test
    void isTokenValidShouldReturnTrueWhenJwtValid() {
        String token = Jwts
                .builder()
                .setSubject(userEntity.getId().toString())
                .claim("role", userEntity.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 500000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Assertions.assertTrue(jwtService.isTokenValid(token, userEntity));
    }

    @Test
    void isTokenValidShouldReturnFalseWhenJwtInvalid() {
        String token = Jwts
                .builder()
                .setSubject(userEntity.getUsername())
                .claim("role", userEntity.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() - 500000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Assertions.assertFalse(jwtService.isTokenValid(token, userEntity));
    }

    @Test
    void isTokenExpiredShouldReturnFalseWhenJwtNotExpired() {
        String token = Jwts
                .builder()
                .setSubject(userEntity.getUsername())
                .claim("role", userEntity.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 500000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Assertions.assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenExpiredShouldReturnTrueWhenJwtExpired() {
        String token = Jwts
                .builder()
                .setSubject(userEntity.getUsername())
                .claim("role", userEntity.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(Instant.now()))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Assertions.assertTrue(jwtService.isTokenExpired(token));
    }

    @Test
    void checkIfRefreshTokenExistsByUserIdShouldSucceed() {
        Mockito.when(refreshTokenRepository.existsByUserId(any())).thenReturn(true);
        boolean result = jwtService.checkIfRefreshTokenExistsByUserId(1L);
        Assertions.assertTrue(result);
    }

    @Test
    void saveRefreshTokenShouldSucceed() {
        Mockito.when(refreshTokenRepository.save(any())).thenReturn(refreshTokenEntity);
        RefreshTokenEntity result = jwtService.saveRefreshToken(userEntity);
        Assertions.assertEquals(refreshTokenEntity, result);
    }

    @Test
    void findRefreshTokenEntityByTokenShouldSucceed() {
        Mockito.when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(refreshTokenEntity));
        RefreshTokenEntity result = jwtService.findRefreshTokenEntityByToken("");
        Assertions.assertEquals(refreshTokenEntity, result);
    }

    @Test
    void findRefreshTokenEntityByTokenShouldFail() {
        Mockito.when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());
        Assertions.assertThrows(RefreshTokenNotFoundException.class, () -> jwtService.findRefreshTokenEntityByToken(""));
    }
}
