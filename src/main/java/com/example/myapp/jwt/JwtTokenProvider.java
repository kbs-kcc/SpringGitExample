package com.example.myapp.jwt;

import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.example.myapp.member.model.Member;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class JwtTokenProvider {

    private static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY");
    
    private static final byte[] decodedKey = Decoders.BASE64.decode(SECRET_KEY);
    private static final SecretKey key = new SecretKeySpec(decodedKey, "HmacSHA256");

    private static final String AUTH_HEADER = "X-AUTH-TOKEN";
    private static final long tokenValidTime = 30 * 60 * 1000L; 
    @Autowired
    private UserDetailsService userDetailsService;

    
    public String generateToken(Member member) {
        long now = System.currentTimeMillis();

        Claims claims = Jwts.claims()
                .subject(member.getUserid())       // sub
                .issuer(member.getName())          // iss
                .issuedAt(new Date(now))           // iat
                .expiration(new Date(now + tokenValidTime)) // exp
                .add("roles", member.getRole())    // roles
                .build();

        return Jwts.builder()
                .claims(claims)
                .signWith(key)   
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader(AUTH_HEADER);
    }

    
    private Claims parseClaims(String token) {
        log.info(token);
        return Jwts.parser()
                .verifyWith(key)    
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUserId(token));
        log.info(userDetails.getUsername());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
