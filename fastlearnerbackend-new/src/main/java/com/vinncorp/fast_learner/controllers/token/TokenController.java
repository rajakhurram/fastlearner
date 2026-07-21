package com.vinncorp.fast_learner.controllers.token;

import com.vinncorp.fast_learner.services.token.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vinncorp.fast_learner.util.Constants.APIUrls;

@RestController
@RequestMapping(APIUrls.TOKEN_API)
@RequiredArgsConstructor
public class TokenController {
    private final ITokenService tokenService;


    @DeleteMapping(APIUrls.DELETE_TOKEN)
    ResponseEntity<String> deleteExpiredTokens(){
        this.tokenService.deleteExpiredTokens();
        return ResponseEntity.status(HttpStatus.OK).body("deleted tokens successfully");
    }

}
