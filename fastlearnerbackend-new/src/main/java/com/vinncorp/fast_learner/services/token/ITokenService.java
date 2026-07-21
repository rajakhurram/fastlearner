package com.vinncorp.fast_learner.services.token;


import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.token.Token;
import com.vinncorp.fast_learner.models.user.User;

public interface ITokenService {
    void create(String username, String token) throws EntityNotFoundException, InternalServerException;

    Token fetchByToken(String token) ;

    boolean existsByToke(String token);

    Token fetchTokenByUserId(Long id);

    void save(Token token) throws InternalServerException;
    void deleteExpiredTokens();
}
