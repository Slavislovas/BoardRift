package com.socialnetwork.boardrift.service;

import com.socialnetwork.boardrift.repository.RefreshTokenRepository;
import com.socialnetwork.boardrift.repository.model.RefreshTokenEntity;
import com.socialnetwork.boardrift.repository.model.user.UserEntity;
import com.socialnetwork.boardrift.util.exception.RefreshTokenNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Setter
@Service
public class JwtService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.secret.key}")
    private String jwtSecretKey;

    @Value("${jwt.access-token.lifetime-in-milliseconds}")
    private Integer jwtAccessTokenLifetimeInMilliseconds;

    @Value("${jwt.refresh-token.lifetime-in-milliseconds}")
    private Integer jwtRefreshTokenLifetimeInMilliseconds;

    public String extractId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserEntity userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserEntity userDetails) {
        return buildToken(extraClaims, userDetails, jwtAccessTokenLifetimeInMilliseconds);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserEntity userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getId().toString())
                .claim("role", userDetails.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean isTokenValid(String token, UserEntity userDetails) {
        try {
            final String id = extractId(token);
            return (id.equals(userDetails.getId().toString()) && !isTokenExpired(token));
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception ex) {
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean checkIfRefreshTokenExistsByUserId(Long id) {
        return refreshTokenRepository.existsByUserId(id);
    }

    public RefreshTokenEntity saveRefreshToken(UserEntity userEntity) {
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(null, new Date(System.currentTimeMillis() + jwtRefreshTokenLifetimeInMilliseconds), userEntity);
        return refreshTokenRepository.save(refreshTokenEntity);
    }

    public void deleteRefreshTokenByUserId(Long id) {
        refreshTokenRepository.deleteByUserId(id);
    }

    public RefreshTokenEntity findRefreshTokenEntityByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElseThrow(RefreshTokenNotFoundException::new);
    }

    public void deleteRefreshTokenByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
