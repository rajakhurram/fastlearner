package com.vinncorp.fast_learner.services.otp;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.otp.AuthenticationOtp;
import com.vinncorp.fast_learner.repositories.otp.AuthenticationRepository;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationOtpService implements IAuthenticationOtpService{

    private final AuthenticationRepository authenticationRepository;
    public static final int OTP_EXPIRY_TIME = 5;

    @Override
    public AuthenticationOtp saveAuthenticationOtp(AuthenticationOtp authenticationOtp) {
        AuthenticationOtp existOtp = this.authenticationRepository.findByEmail(authenticationOtp.getEmail());
        if(!Objects.isNull(existOtp)){
            this.authenticationRepository.deleteById(existOtp.getId());
        }
        return this.authenticationRepository.save(authenticationOtp);
    }

    @Override
    public AuthenticationOtp verifyAuthenticationOtp(String email, int otp) throws BadRequestException {
        AuthenticationOtp existOtp = this.authenticationRepository.findByEmail(email);
        if(existOtp != null && existOtp.getOtp() == otp){
            long minutesDifference = ChronoUnit.MINUTES.between(existOtp.getLocalDateTime(), LocalDateTime.now());
            return minutesDifference < OTP_EXPIRY_TIME ? existOtp : null;
        }else{
            throw new BadRequestException("Invalid OTP");
        }
    }

    @Override
    public Message<String> resendAuthenticationOtp(String email, int otp) throws EntityNotFoundException {
        AuthenticationOtp existOtp = this.authenticationRepository.findByEmail(email);
        if(!Objects.isNull(existOtp)){
            existOtp.setOtp(otp);
            this.authenticationRepository.save(existOtp);
            return new Message<String>()
                    .setData("Authentication OTP resend successfully.")
                    .setMessage("Authentication OTP resend successfully.")
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString());
        }
        throw new EntityNotFoundException("User ot found: "+email);
    }
}
