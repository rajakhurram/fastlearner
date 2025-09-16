package com.vinncorp.fast_learner.controllers.auth;

import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.*;
import com.vinncorp.fast_learner.request.auth.LocalAuthRequest;
import com.vinncorp.fast_learner.request.auth.LocalRegisterRequest;
import com.vinncorp.fast_learner.request.auth.PasswordResettingRequest;
import com.vinncorp.fast_learner.response.auth.JwtTokenResponse;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.services.auth.IAuthenticationService;
import com.vinncorp.fast_learner.services.notification.INotificationService;
import com.vinncorp.fast_learner.services.token.ITokenService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.AUTHENTICATION_MAIN)
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthenticationService service;
    private final ITokenService tokenService;
    private final INotificationService notificationService;

    @PostMapping(APIUrls.SOCIAL_LOGIN)
    public ResponseEntity<TokenResponse> socialAuthentication(@RequestParam String token, @RequestParam String provider,
                                                              @RequestParam(required = false) String clientType,
                                                              @RequestParam(required = false) String userName)
            throws AuthenticationException, InternalServerException, BadRequestException {
        TokenResponse m = service.socialLogin(token, provider, clientType, userName);
        return ResponseEntity.ok(m);
    }

    @PostMapping(APIUrls.LOCAL_LOGIN)
    public ResponseEntity<TokenResponse> localAuthentication(@Valid @RequestBody LocalAuthRequest request) throws BadRequestException, AuthenticationException, EntityNotFoundException {
        TokenResponse token = service.localLogin(request);
        return ResponseEntity.ok(token);
    }

    @GetMapping(APIUrls.TOKEN_VALIDATION)
    public ResponseEntity<Message> tokenValidation(HttpServletRequest request, Principal principal) throws EntityNotFoundException {
        return ResponseEntity.ok(service.tokenValidation(JwtUtils.parseJwt(request), principal));
    }

    @GetMapping(APIUrls.DO_REFRESH_TOKEN)
    public ResponseEntity<JwtTokenResponse> doRefresh(HttpServletRequest request, Principal principal) throws EntityNotFoundException {
        return ResponseEntity.ok(service.refreshToken(JwtUtils.parseJwt(request), principal));
    }

    @PostMapping(APIUrls.DO_LOGOUT)
    public ResponseEntity doLogout(@RequestParam(required = false) String uniqueId, HttpServletRequest request, Principal principal) throws InternalServerException, EntityNotFoundException {
        tokenService.create(principal.getName(), JwtUtils.parseJwt(request));
        notificationService.deleteEmitter(principal.getName(), uniqueId);
        return ResponseEntity.ok(null);
    }

    @PostMapping(APIUrls.LOCAL_REGISTER)
    public ResponseEntity<TokenResponse> localRegistration(@Valid @RequestBody LocalRegisterRequest request) throws InternalServerException, EntityAlreadyExistException {
        TokenResponse token = service.localRegister(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping(APIUrls.SENDING_LINK_RESET_PASSWORD)
    public ResponseEntity<Message<String>> resetPassword(@RequestParam String email) throws EntityNotFoundException, MessagingException {
//        var m = service.sendingLinkForResettingPassword(email);
        var m = service.sendLinkForResettingPassword(email);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.RE_SENDING_LINK_RESET_PASSWORD)
    public ResponseEntity<Message<String>> resendingResetPassword(@RequestParam String email) throws EntityNotFoundException, MessagingException {
//        var m = service.sendingLinkForResettingPassword(email);
        var m = service.reSendLinkForResettingPassword(email);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.RESET_PASSWORD)
    public ResponseEntity<Message<String>> resetPassword(@RequestBody PasswordResettingRequest request)
            throws BadRequestException, EntityNotFoundException, InternalServerException {
        var m = service.resetPassword(request.getPassword(), request.getEmail());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.AUTHENTICATION_OTP)
    ResponseEntity<Message<String>> authenticationOtp(@Valid @RequestBody LocalRegisterRequest request) throws EntityNotFoundException, EntityAlreadyExistException {
        var m = this.service.authenticationOtp(request);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.VERIFY_AUTHENTICATION_OTP)
    ResponseEntity<TokenResponse> verifyAuthenticationOtp(@RequestParam String email, @RequestParam int otp) throws BadRequestException, EntityAlreadyExistException, InternalServerException {
        var m = this.service.verifyAuthenticationOtp(email, otp);
        return ResponseEntity.ok(m);
    }

    @PostMapping(APIUrls.RESEND_AUTHENTICATION_OTP)
    ResponseEntity<Message<String>> resendAuthenticationOtp(@RequestParam String email) throws EntityNotFoundException, BadRequestException {
        var m = this.service.resendAuthenticationOtp(email);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.VERIFY_OTP)
    ResponseEntity<Message<String>> verifyOtp(@RequestParam String email, @RequestParam int otp) throws BadRequestException, EntityNotFoundException {
        var m = this.service.verifyingOtp(email, otp);
        return ResponseEntity.ok(m);
    }

    @GetMapping(APIUrls.VERIFY_TOKEN)
    public ResponseEntity<?> verifyToken(HttpServletRequest request, Principal principal) {
        return ResponseEntity.ok(principal.getName());
    }

    @PostMapping(APIUrls.DISABLE_OR_DELETE_ACCOUNT)
    public ResponseEntity<Message<String>> disableOrDeleteAccount(Principal principal, @RequestParam String action)
            throws EntityNotFoundException, BadRequestException, InternalServerException {
        Message<String> response = service.handleUserAccount(principal.getName(), action);
        return ResponseEntity.ok(response);
    }


}
