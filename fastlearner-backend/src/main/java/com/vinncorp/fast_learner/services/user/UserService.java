package com.vinncorp.fast_learner.services.user;

import com.vinncorp.fast_learner.dtos.user.ChangePasswordRequestDto;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.role.Role;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.user.UserCreationRequest;
import com.vinncorp.fast_learner.controllers.youtube_video.user.UserCreationResponse;
import com.vinncorp.fast_learner.response.course.CourseDetailResponse;
import com.vinncorp.fast_learner.response.instructor.InstructorPaginatedResponse;
import com.vinncorp.fast_learner.response.instructor.InstructorResponse;
import com.vinncorp.fast_learner.response.user.UserResponse;
import com.vinncorp.fast_learner.services.email_template.IEmailService;
import com.vinncorp.fast_learner.services.role.IRoleService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.template.NotifyLoginEmail;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.AuthProvider;
import com.vinncorp.fast_learner.util.enums.PaymentStatus;
import com.vinncorp.fast_learner.util.enums.UserRole;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service

public class UserService implements IUserService, UserDetailsService {

    private static final String DEFAULT_SUPPORT_EMAIL = "support-fastlearner@mailinator.com";
    @Value("${support.email:" + DEFAULT_SUPPORT_EMAIL + "}")
    private String SUPPORT_EMAIL;
    private final UserRepository repository;
    private final IRoleService roleService;
    private final ISubscriptionService subscriptionService;
    private final ISubscribedUserService subscribedUserService;
    private final IEmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, IRoleService roleService,
                       @Lazy ISubscriptionService subscriptionService,
                       ISubscribedUserService subscribedUserService, IEmailService emailService, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleService = roleService;
        this.subscriptionService = subscriptionService;
        this.subscribedUserService = subscribedUserService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Logging into the system.");
        User user = repository.findByEmail(username).orElseThrow(() -> {
            log.error("Invalid credentials.");
            return new UsernameNotFoundException("Invalid credentials.");
        });
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if(Objects.nonNull(user.getRole()))
            authorities = getAuthorities(user.getRole().getType());
        return new org.springframework.security.core.userdetails.User(
                username, user.getPassword() == null ? "no-password" : user.getPassword(), user.isActive(),
                true, true, true, authorities
        );
    }

    public Collection<GrantedAuthority> getAuthorities(String role) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if(UserRole.STUDENT.name().equals(role)) {
            GrantedAuthority adminAuthority = UserRole.STUDENT::name;
            grantedAuthorities.add(adminAuthority);
        }else if(UserRole.INSTRUCTOR.name().equals(role)) {
            GrantedAuthority officerAuthority = UserRole.INSTRUCTOR::name;
            grantedAuthorities.add(officerAuthority);
        }
        return grantedAuthorities;
    }

    @Override
    public Message<UserCreationResponse> create(UserCreationRequest request) throws EntityNotFoundException, InternalServerException {
        log.info("Creating user with email: " + request.getEmail());
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(roleService.getByType(request.getRole()))
                .provider(AuthProvider.valueOf(request.getProvider()))
                .creationDate(new Date())
                .isActive(true)
                .build();

        try {
            user = repository.save(user);
            log.info("User created successfully.");
        } catch (Exception e) {
            log.error("ERROR: "+e.getLocalizedMessage());
            throw new InternalServerException("Error occurred on server side.");
        }

        return new Message<UserCreationResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("User created successfully.")
                .setData(UserCreationResponse.fromUser(user));
    }

    @Override
    public User findByEmail(String email) throws EntityNotFoundException {
        log.info("Fetching user by email: "+ email.trim().toLowerCase());
        return repository.findByEmail(email.trim().toLowerCase()).orElseThrow(() -> {
            log.error("User not found.");
            return new EntityNotFoundException("User not found.");
        });
    }

    @Override
    public User save(User user) throws InternalServerException {
        log.info("Saving user.");
        try {
            return repository.save(user);
        } catch (Exception e) {
            log.error("ERROR: "+ e.getLocalizedMessage());
            throw new InternalServerException("User"+ InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Message<String> addRoleForUser(UserRole valueOf, String name) throws EntityNotFoundException, InternalServerException {
        log.info("Adding role into user: " + name);
        User user = findByEmail(name);
        Role role = roleService.getByType(valueOf.name());
        if (UserRole.INSTRUCTOR == valueOf) {
            // then subscribe for free subscription
            Subscription freeSubscription = subscriptionService.findBySubscriptionId(1L).getData();
            SubscribedUser subscribedUser = SubscribedUser.builder()
                    .subscription(freeSubscription)
                    .user(user)
                    .paymentStatus(PaymentStatus.PAID)
                    .startDate(new Date())
                    .isActive(true)
                    .build();
            subscribedUserService.save(subscribedUser);
            user.setSubscribed(true);
        }
        user.setRole(role);
        save(user);
        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Role added successfully.")
                .setData("Role added successfully.");
    }

    @Override
    public Message<String> changePassword(ChangePasswordRequestDto requestDto, Principal principal) throws EntityNotFoundException, BadRequestException, InternalServerException {
        log.info("Change password request.");

        //get logged in user
        User user = findByEmail(principal.getName());

        if(!passwordEncoder.matches(requestDto.getOldPassword(),user.getPassword())){
            log.error("ERROR: Current password does not match");
            throw new BadRequestException("Current password does not match");
        }

        //set new password
        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        user = save(user);

        return new Message<String>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Password changed successfully");

    }

    public User getUserByEmail(String email){
        Optional<User> user = this.repository.findByEmail(email);
        return user.isPresent() ? user.get() : null;
    }

    @Override
    public List<User> findAllUsersNotLoggedInForTenDays() {
        log.info("Fetching users who have not logged in for 10 or 15 days");
        List<Tuple> userList;
        List<User> usersList=new ArrayList<>();
        try {

            userList=this.repository.findAllUsersNotLoggedInForTenDays();
            if (userList.size()>0) {

                userList.stream().map(u -> {
                    User users = new User();
                    users.setId((Long) u.get("id"));
                    users.setEmail((String) u.get("email"));
                    users.setFullName((String) u.get("full_name"));
                    users.setLoginTimestamp((Date) u.get("login_timestamp"));
                    usersList.add(users);
                    return users;

                }).collect(Collectors.toList());

                usersList.stream().forEach(user -> {
                    String body = NotifyLoginEmail.loginEmailTemplate(user.getFullName(),user.getLoginTimestamp());
                    emailService.sendEmail(user.getEmail(), "We Miss You! Ready to Continue Your Learning Journey?", body, true);
                    log.info("Email Sent Successfully " + user.getEmail());
                });
            }else {
                log.info("No users found who have been inactive for 10 or 15 days.");
            }
        }catch (Exception e){
            log.error("ERROR: "+ e.getLocalizedMessage());
        }
        return usersList;
    }

    @Override
    public User findById(Long id) throws EntityNotFoundException {
        log.info("Fetching user by id: "+id);
        return repository.findById(id).orElseThrow(() -> {
            log.error("User not found.");
            return new EntityNotFoundException("User not found.");
        });
    }

    @Override
    public Message<UserResponse> getUserDetail(String name) {
        log.info("Checking if instructor exists with email: {}", name);
        Optional<User> user = repository.findByEmail(name);
        if (!user.isPresent()) {
            log.info("Instructor not found with email: {}", name);
            return new Message<UserResponse>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor not found with email: " + name);
        }
        UserResponse userResponse=UserResponse.builder()
                .email(user.get().getEmail())
                .name(user.get().getFullName()).build();



        return new Message<UserResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("User Details Found Successfully " )
                .setData(userResponse);
    }


    public Message<InstructorPaginatedResponse> getAllTopInstructor(Pageable pageable) {
        List<InstructorResponse> listOfInstructor = new ArrayList<>();
        Page<Tuple> data;
        try {
            data = repository.findAllTopInstructor(pageable);

            if (data.hasContent()) {
                log.info("Successfully fetched top instructor data");
                listOfInstructor = data.stream()
                        .map(InstructorResponse::from)
                        .collect(Collectors.toList());
            } else {
                log.info("No top instructors found for the requested page.");
            }

        } catch (Exception e) {
            log.warn("Failed to fetch top instructor. Reason: {}", e.getMessage());
            return new Message<InstructorPaginatedResponse>()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .setMessage("An error occurred while top instructor.");
        }
        InstructorPaginatedResponse response = InstructorPaginatedResponse.builder()
                .data(listOfInstructor)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .pages(data.getTotalPages())
                .totalElements(data.getTotalElements())
                .build();
        // Manually set the parent fields (previousPage, currentPage, nextPage)
        response.setPreviousPage(pageable.getPageNumber() > 0 ? (long) pageable.getPageNumber() - 1 : null);
        response.setCurrentPage((long) pageable.getPageNumber());
        response.setNextPage(pageable.getPageNumber() < data.getTotalPages() - 1 ? (long) pageable.getPageNumber() + 1 : null);

        return new Message<InstructorPaginatedResponse>()
                .setData(response)
                .setMessage("All top instructor fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }

}
