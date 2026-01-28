package com.vinncorp.fast_learner.services.user_session;

import com.vinncorp.fast_learner.config.JwtUtils;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user_session.UserSession;
import com.vinncorp.fast_learner.repositories.subscription.SubscriptionRepository;
import com.vinncorp.fast_learner.repositories.user_session.UserSessionRepository;
import com.vinncorp.fast_learner.response.auth.TokenResponse;
import com.vinncorp.fast_learner.response.user_session.UserSessionResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionService implements IUserSessionService{
    @Value("${expiry.time.for.user.session.expiration}")
    private long EXPIRY_TIME_FOR_USER_SESSION_EXPIRATION;
    @Value("${jwt.app.token.expiration.in.ms}")
    private long EXPIRATION_TIME_IN_MILLI;

    private final UserSessionRepository userSessionRepository;

    private final TaskScheduler scheduler;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtUtils jwtUtils;
    private final IUserService userService;
    private final ICourseService courseService;
    private final ICourseUrlService courseUrlService;

    @Override
    public Message createSessionId(Long subscriptionId, Long courseId, Principal principal) throws EntityNotFoundException, BadRequestException {
        Optional<Subscription> subscription = null;
        Course course = null;
        if(Objects.isNull(subscriptionId) && Objects.isNull(courseId)){
            throw new BadRequestException("Required parameters are missing");
        }

        log.info("Checking if user exists with email: {}", principal.getName());
        User user = this.userService.findByEmail(principal.getName());

        String sessionId = UUID.randomUUID().toString();
        log.info("Generated new session ID: {}", sessionId);

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(user.getId())
                .createdAt(LocalDateTime.now())
                .build();

        if(Objects.nonNull(subscriptionId)){
            log.info("Checking if subscription exists with ID: {}", subscriptionId);
            subscription = subscriptionRepository.findById(subscriptionId);
            if (!subscription.isPresent()) {
                log.info("Subscription not found with ID: {}", subscriptionId);
                return new Message<>()
                        .setStatus(HttpStatus.NOT_FOUND.value())
                        .setCode(HttpStatus.NOT_FOUND.toString())
                        .setMessage("Subscription not found with this ID: " + subscriptionId);
            }
            log.info("Subscription found with ID: {}", subscriptionId);
            session.setSubscription(subscription.get());
        }else {
            course = this.courseService.findById(courseId);
            if(Objects.isNull(course)){
                throw new EntityNotFoundException("course not found by given id: "+courseId);
            }
            session.setCourseId(courseId);
            session.setCoursePrice(course.getPrice());
            session.setCourseUrl(this.courseUrlService.findActiveUrlByCourseIdAndStatus(courseId, GenericStatus.ACTIVE).getUrl());
        }

        log.info("Saving user session for user ID: {}", user.getId());

        final UserSession userSession = userSessionRepository.save(session);
        log.info("User session saved with session ID: {}", userSession.getSessionId());

        scheduler.schedule(() -> {
            log.info("User session expired, deleting the session ID: {}", userSession.getSessionId());
            userSessionRepository.delete(userSession);
            log.info("User session ID deleted successfully.");
        }, new Date(System.currentTimeMillis() + EXPIRY_TIME_FOR_USER_SESSION_EXPIRATION));

        log.info("Returning response with session ID: {}", userSession.getSessionId());
        return new Message<>()
                .setData(userSession.getSessionId())
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Session id created Successfully");
    }

    public Message<UserSessionResponse> generateTokenAgainstSessionId(String sessionId) throws BadRequestException, EntityNotFoundException, InternalServerException {
        if(Objects.isNull(sessionId)){throw new BadRequestException("required parameters are missing");}
        UserSession userSession = this.userSessionRepository.findBySessionId(sessionId);
        if(Objects.isNull(userSession)){throw new EntityNotFoundException("No data found");}
        User user = this.userService.findById(userSession.getUserId());
        try {
            log.info("generating token against session id");
            return new Message<UserSessionResponse>()
                    .setData(UserSessionResponse
                            .builder()
                            .subscriptionId(Objects.nonNull(userSession.getSubscription()) ? userSession.getSubscription().getId() : null)
                            .courseId(userSession.getCourseId())
                            .coursePrice(userSession.getCoursePrice())
                            .courseUrl(userSession.getCourseUrl())
                            .tokenResponse(
                                    TokenResponse.builder()
                                            .token(this.jwtUtils.generateJwtToken(user.getEmail(), user))
                                            .refreshToken(this.jwtUtils.doGenerateRefreshToken(user.getEmail(), user))
                                            .expiredInSec((int) EXPIRATION_TIME_IN_MILLI / 1000)
                                            .name(user.getFullName())
                                            .email(user.getEmail())
                                            .role(user.getRole() == null ? null : user.getRole().getType())
                                            .isSubscribed(user.isSubscribed())
                                            .build()
                            )
                            .build()
                            )
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString())
                    .setMessage("Token fetched Successfully");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR.name());
        }
    }

}
