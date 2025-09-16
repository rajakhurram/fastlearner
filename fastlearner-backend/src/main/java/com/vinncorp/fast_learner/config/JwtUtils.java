package com.vinncorp.fast_learner.config;

import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.Constants.Text;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;


@Component
@Getter
@Setter
public class JwtUtils {
    @Value("${jwt.app.secret}")
    private String jwtSecret;

    @Value("${jwt.app.token.expiration.in.ms}")
    private long jwtExpirationMs;

    @Value("${jwt.app.refresh.expiration.in.ms}")
    private long refreshExpirationDateInMs;

    @Value("${jwt.app.encryption.key}")
    private String JWT_ENCRYPTION_KEY;

    public long getJwtExpirationMs() {
        return this.jwtExpirationMs;
    }

    public String generateJwtToken(String name, User user) {
        Map<String, Object> claims = new HashMap<>();
        if(Objects.nonNull(user.getRole()))
            claims.put(user.getRole().getType(), user.getRole().getType());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(name)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public String doGenerateRefreshToken(String subject, User user) {
        Map<String, Object> claim = new HashMap<>();
        if(Objects.nonNull(user.getRole()))
            claim.put(user.getRole().getType(), user.getRole().getType());
        return Jwts.builder().setClaims(claim).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationDateInMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret).compact();

    }

    public boolean validateToken(String authToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException(Text.INVALID_CREDENTIALS, ex);
        } catch (ExpiredJwtException ex) {
            throw ex;
        }
    }

    public static String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(Text.BEARER)) {
            return headerAuth.substring(7, headerAuth.length());
        }

        return null;
    }

    /**
     * Encrypts the provided plaintext using AES encryption.
     *
     * @param plaintext The string to be encrypted.
     * @return The encrypted string.
     * @throws Exception If an error occurs during encryption.
     */
    public String encrypt(String plaintext) throws InternalServerException {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(JWT_ENCRYPTION_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerException("Encryption is not working.");
        }
    }

    public boolean isTokenExpired(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return false;
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException(Text.INVALID_CREDENTIALS, ex);
        }
    }

}
