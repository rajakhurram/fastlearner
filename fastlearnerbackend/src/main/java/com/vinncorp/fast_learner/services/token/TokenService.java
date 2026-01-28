package com.vinncorp.fast_learner.services.token;

import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.token.TokenRepository;
import com.vinncorp.fast_learner.models.token.Token;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService{

    private final IUserService userService;
    private final TokenRepository repo;
    private final JwtUtils jwtUtils;

    @Override
    public void create(String username, String tokenString) throws EntityNotFoundException, InternalServerException {
        log.info("Saving token for user: "+username);
        User user = userService.findByEmail(username);
        Token token = new Token();
        token.setToken(tokenString);
        token.setCreatedBy(user.getId());
        token.setCreationDate(new Date());
        try {
            repo.save(token);
            log.info("Token successfully saved in database for user: "+username);
        } catch (Exception e) {
            throw new InternalServerException("Token not saved in database for user: "+username);
        }
    }

    @Override
    public Token fetchByToken(String token) {
        log.info("Fetching token expired token.");
        Token savedToken = repo.findByToken(token);
        if (savedToken == null) {
            log.info("Token is not found");
            return null;
        }
        log.info("Token successfully fetched.");
        return savedToken;
    }

    @Override
    public boolean existsByToke(String token) {
        //fo("Is token exists by provided token.");
        if(token == null || token.isBlank())
            return false;
        boolean exists = repo.existsByToken(token);
        if (exists) {
            //logService.info("Token exists.");
        }

        return exists;
    }

    @Override
    public Token fetchTokenByUserId(Long id) {
        Token token = repo.findByCreatedBy(id);
        return token;
    }

    @Override
    public void save(Token token) throws InternalServerException {
        try {
            repo.save(token);
            log.info("Token updated successfully in the database.");
        } catch (Exception e) {
            throw new InternalServerException("Token not updated successfully in the database.");
        }
    }

    @Scheduled(cron = "0 0 12 * * SUN")
    public void deleteExpiredTokens() {
        log.info("Deleting the expired tokens");

        List<Token> tokens = this.repo.findAll();

        if (!tokens.isEmpty()) {
            List<Long> expiredTokensId = new ArrayList<>();
            try {
                tokens.stream()
                        .filter(token -> this.jwtUtils.isTokenExpired(token.getToken()))
                        .forEach(expiredToken -> {
                            expiredTokensId.add(expiredToken.getId());
                            this.repo.deleteById(expiredToken.getId());
                        });
            }catch(Exception e){
                log.info(InternalServerException.NOT_DELETE_INTERNAL_SERVER_ERROR + e.getMessage());
            }
            log.info(expiredTokensId.stream().count() + " deleted at "+ LocalDateTime.now());
        }
    }

}
