package com.vinncorp.fast_learner.services.otp;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.otp.Otp;
import com.vinncorp.fast_learner.repositories.otp.AuthenticationRepository;
import com.vinncorp.fast_learner.repositories.otp.OtpRepository;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService implements IOtpService{

    @Value("${expiry.time.for.password.reset.otp}")
    private long EXPIRY_TIME_FOR_PASSWORD_RESET_OTP;

    private final OtpRepository otpRepository;
    private final TaskScheduler scheduler;

    private final IUserService userService;

    private final AuthenticationRepository authenticationRepository;

    @Override
    public int createOtp(User user) {
        log.info("Creating otp for user: "+user.getFullName());
        Random random = new Random();
        Otp otp = new Otp();
        int min = 100000;
        int max = 999999;
        int value = random.nextInt(max - min + 1) + min;
        otp.setValue(value);
        otp.setUser(user);

        final Otp savedOtp = otpRepository.save(otp);
        scheduler.schedule(() -> {
            log.info("OTP expired, deleting the otp");
            otpRepository.delete(savedOtp);
            log.info("OTP deleted.");
        }, new Date(System.currentTimeMillis() + EXPIRY_TIME_FOR_PASSWORD_RESET_OTP));
        return savedOtp.getValue();
    }

    @Override
    public boolean verifyingOtp(long id, int value) throws BadRequestException {
        log.info("Verifying OTP.");
        Otp otp = otpRepository.findByUserIdAndValue(id, value);
        if(otp == null) {
            log.error("Invalid OTP provided.");
            throw new BadRequestException("Invalid OTP provided.");
        }
        otpRepository.delete(otp);
        log.info("OTP verified.");
        return true;
    }

    @Override
    public int createAuthenticationOtp() {
        int min = 100000;
        int max = 999999;
        Random random = new Random();
        int otp = random.nextInt(max - min + 1) + min;
        if(!Objects.isNull(authenticationRepository.findByOtp(otp))){
            this.createAuthenticationOtp();
        }
        return otp;
    }



    @Override
    public Boolean verifyOtp(String email, int otp) throws EntityNotFoundException {

        log.info("Finding user by email: {}", email);
        User user = userService.findByEmail(email);

        if (user == null) {
            log.warn("No user found with email: {}", email);
            return false;
        }

        log.info("Attempting to find OTP for user ID: {}", user.getId());
        Otp verifyOtp = otpRepository.findByUserIdAndValue(user.getId(), otp);

        if (verifyOtp != null) {
            log.info("OTP verified successfully for user ID: {}", user.getId());
            return true;
        } else {
            log.warn("Invalid OTP for user ID: {}", user.getId());
            return false;
        }
    }

    @Override
    public Boolean findByUserId(User user) {
       Optional<Otp> isExistOtp=otpRepository.findByUserId(user.getId());
       if (isExistOtp.isPresent()){
           otpRepository.delete(isExistOtp.get());
           return true;
       }
       return false;
    }
}
