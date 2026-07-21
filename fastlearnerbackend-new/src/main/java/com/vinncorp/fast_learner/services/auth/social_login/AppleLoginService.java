package com.vinncorp.fast_learner.services.auth.social_login;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.AuthenticationException;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.response.auth.social_login.TokenInfoResponse;
import com.vinncorp.fast_learner.services.role.IRoleService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.enums.AuthProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Date;

@Slf4j
@Service
public class AppleLoginService implements ISocialLoginService {

    private static final String ISSUER = "https://appleid.apple.com";
    private static final String AUDIENCE = "com.vinncorp.fastlearner";
    private static final String LOAD_URL = "https://appleid.apple.com/auth/keys";

    private final RestTemplate restTemplate;
    private final IUserService userService;
    private final IRoleService roleService;
    private final JwtUtils jwtUtils;
    private final IUserProfileService userProfileService;

    public AppleLoginService(RestTemplate restTemplate, IUserService userService, IRoleService roleService, JwtUtils jwtUtils, IUserProfileService userProfileService) {
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.roleService = roleService;
        this.jwtUtils = jwtUtils;
        this.userProfileService = userProfileService;
    }

    @Override
    public TokenResponse login(String token, String CLIENT_ID, String userName) throws AuthenticationException, InternalServerException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWKSet jwkSet = JWKSet.load(new URL(LOAD_URL));
            JWSHeader header = signedJWT.getHeader();
            JWK jwk = jwkSet.getKeyByKeyId(header.getKeyID());

            if (jwk == null) {
                throw new BadRequestException("Invalid key ID");
            }

            JWSVerifier verifier = new RSASSAVerifier(((RSAKey) jwk).toRSAPublicKey());
            if (!signedJWT.verify(verifier)) {
                throw new InternalServerException("JWT signature verification failed");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (!claims.getIssuer().equals(ISSUER) || !claims.getAudience().contains(AUDIENCE)) {
                throw new BadRequestException("Invalid issuer or audience");
            }

            TokenInfoResponse tokenInfoResponse = new TokenInfoResponse();
            tokenInfoResponse.setName(userName);
            tokenInfoResponse.setEmail(claims.getStringClaim("email"));

            User savedUser = registerUserIfNotAlreadyRegistered(tokenInfoResponse);

            // Generate JWT tokens
            String jwtToken = jwtUtils.generateJwtToken(savedUser.getEmail(), savedUser);
            String refreshToken = jwtUtils.doGenerateRefreshToken(savedUser.getEmail(), savedUser);

            return TokenResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .expiredInSec((int) jwtUtils.getJwtExpirationMs() / 1000)
                    .name(savedUser.getFullName())
                    .email(savedUser.getEmail())
                    .role(savedUser.getRole() == null ? null : savedUser.getRole().getType())
                    .isSubscribed(savedUser.isSubscribed())
                    .build();
        }catch(Exception e){
            throw new InternalServerException(InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    public User registerUserIfNotAlreadyRegistered(TokenInfoResponse userInfo) throws InternalServerException {
        User user = null;
        try {
            user = userService.findByEmail(userInfo.getEmail());
            return user;
        } catch (EntityNotFoundException e) {
            log.info("User not found in database, Registering new user.");
        }
        user = userService.save(User.builder()
                .email(userInfo.getEmail())
                .fullName(userInfo.getName())
                .provider(AuthProvider.APPLE)
                .salesRaise(1.0)
                .isActive(true)
                .loginTimestamp(new Date())
                .creationDate(new Date())
                .build());

        UserProfile userProfile = new UserProfile();
        userProfile.setCreationDate(new Date());
        userProfile.setCreatedBy(user.getId());
        userProfileService.createProfile(userProfile, user);

        return user;
    }

}
