package com.socialnetwork.boardrift.unit.service;

import com.socialnetwork.boardrift.enumeration.Role;
import com.socialnetwork.boardrift.enumeration.UserStatus;
import com.socialnetwork.boardrift.repository.RefreshTokenRepository;
import com.socialnetwork.boardrift.repository.model.UserEntity;
import com.socialnetwork.boardrift.service.JwtService;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

@SpringBootTest(properties = {
        "jwt.secret.key=GFR4d2dfg7gESRGZX52dxcbvrDF85qjGGFDS42121BVCXRDFUKJCVBDFGsd2",
        "jwt.access-token.lifetime-in-milliseconds=300000",
        "jwt.refresh-token.lifetime-in-milliseconds=259200000"
})
@ExtendWith({MockitoExtension.class})
public class JwtServiceUnitTests {
    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    @InjectMocks
    JwtService jwtService;

    UserEntity userEntity;

    Key signingKey;

    @BeforeEach
    void init()  {
        userEntity = new UserEntity(1L, "Name", "Lastname", "email@gmail.com",
                "2001-11-16", "Username", "Password@123", true, false, "",
                Role.ROLE_USER, UserStatus.OFFLINE, false, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_SET, Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_SET);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode("GFR4d2dfg7gESRGZX52dxcbvrDF85qjGGFDS42121BVCXRDFUKJCVBDFGsd2"));
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
        Assertions.assertEquals(userEntity.getUsername(), claims.getSubject());
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

        String username = jwtService.extractUsername(token);

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

        Assertions.assertThrows(Exception.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void isTokenValidShouldReturnTrueWhenJwtValid() {
        String token = Jwts
                .builder()
                .setSubject(userEntity.getUsername())
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
}
