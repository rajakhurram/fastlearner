package com.vinncorp.fast_learner.services.affiliate.affiliate_service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Http;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.affiliate.Affiliate;
import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.affiliate.AffiliateRepository;
import com.vinncorp.fast_learner.repositories.affiliate.InstructorAffiliateRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.affiliate.CreateAffiliateReq;
import com.vinncorp.fast_learner.response.affiliate.AffiliateDetailResponse;
import com.vinncorp.fast_learner.response.affiliate.AffiliationResponse;
import com.vinncorp.fast_learner.response.affiliate.AffiliationResponseByPaginated;
import com.vinncorp.fast_learner.services.email_template.EmailService;
import com.vinncorp.fast_learner.template.AffiliateUserEmail;
import com.vinncorp.fast_learner.util.Constants.EmailTemplate;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.TimeUtil;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateService implements IAffiliateService {
    @Value("${stripe.redirect.url}") // Injecting the base redirect URL from the properties file
    private String stripeRedirectUrlTemplate;

    @Value("${stripe.secret.key}")
    private String SECRET_KEY;
    @Value("${stripe.success.url}")
    private String SUCCESS_URL;

    @Value("${stripe.failure.url}")
    private String FAILURE_URL;

    private final UserRepository userRepository;
   private final AffiliateRepository affiliateRepository;
   private final InstructorAffiliateRepository instructorAffiliateRepository;

   private final EmailService emailService;

    @Override
    public Message<InstructorAffiliate> createAffiliateUser(CreateAffiliateReq request, Principal principal) throws StripeException {
        log.info("Starting the process of creating an affiliate user");
        Boolean isStripeAccExist=false;

        // Initialize variables
        InstructorAffiliate instructorAffiliate = new InstructorAffiliate();
        Affiliate affiliate = affiliateRepository.findByEmail(request.getEmail());

        // Check if affiliate exists by email
        if (affiliate == null) {
            log.info("No existing affiliate found with email: {}", request.getEmail());
            affiliate=Affiliate.builder()
                    .email(request.getEmail())
                    .onboardStatus(PayoutStatus.PENDING)
                    .build();
            isStripeAccExist=true;
        } else {
            log.info("Existing affiliate found with email: {}", request.getEmail());
            if (affiliate.getStripeAccountId()==null){
                isStripeAccExist=true;
            }
        }

        // Check if instructor exists
        log.info("Checking if instructor exists with email: {}", principal.getName());
        Optional<User> instructorOpt = userRepository.findByEmail(principal.getName());
        if (!instructorOpt.isPresent()) {
            log.info("Instructor not found with email: {}", principal.getName());
            return new Message<InstructorAffiliate>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor not found with email: " + principal.getName());
        }
        User instructor = instructorOpt.get();
        log.info("Instructor found with ID: {}", instructor.getId());

        log.info("Checking for existing affiliation with instructor ID: {}, and nickname: {}",
                instructor.getId(),request.getNickName());
        Tuple isAffiliateExist= instructorAffiliateRepository.findByInstructorAndAffiliateId(instructor.getId(),affiliate.getId());
        if (isAffiliateExist!=null) {
            String status= (String) isAffiliateExist.get("status");
            log.info("Affiliate user already exists with the given nickname or email");
           if (!GenericStatus.INACTIVE.name().equals(status)){
               return new Message<InstructorAffiliate>()
                       .setStatus(HttpStatus.CONFLICT.value())
                       .setCode(HttpStatus.CONFLICT.toString())
                       .setMessage("Affiliate user already exists with the given nickname or email");
            }else {
                instructorAffiliate = instructorAffiliateRepository.findByInstructorAndNickName(instructor.getId(), request.getNickName());
               if (instructorAffiliate != null) {
                   log.info("Affiliate user already exists with the given nickname");
                   return new Message<InstructorAffiliate>()
                           .setStatus(HttpStatus.CONFLICT.value())
                           .setCode(HttpStatus.CONFLICT.toString())
                           .setMessage("Affiliate user already exists with the given nickname");

               } else {
                   Long instructorAffiliateId= (Long) isAffiliateExist.get("id");
                   instructorAffiliate = instructorAffiliateRepository.findById(instructorAffiliateId).get();
                   if (instructorAffiliate!=null){
                       instructorAffiliate.setLastModifiedDate(new Date());
                       instructorAffiliate.setNickname(request.getNickName());
                       instructorAffiliate.setUsername(request.getName());
                       instructorAffiliate.setDefaultReward(request.getDefaultReward());
                       instructorAffiliate.setStatus(GenericStatus.ACTIVE);
                       instructorAffiliate = instructorAffiliateRepository.save(instructorAffiliate);
                   }
                   }

           }


        }
        InstructorAffiliate isAffiliateNicknameExist=null;
        if (instructorAffiliate.getId()==null ) {
             isAffiliateNicknameExist = instructorAffiliateRepository.findByInstructorAndNickName(instructor.getId(), request.getNickName());
            if (isAffiliateNicknameExist != null) {
                if (isAffiliateNicknameExist.getStatus().equals(GenericStatus.ACTIVE)) {
                    log.info("Affiliate user already exists with the given nickname");
                    return new Message<InstructorAffiliate>()
                            .setStatus(HttpStatus.CONFLICT.value())
                            .setCode(HttpStatus.CONFLICT.toString())
                            .setMessage("Affiliate user already exists with the given nickname");
                }
            }
        }
            if (affiliate.getId() == null) {
                affiliate = affiliateRepository.save(affiliate);
                log.info("Created a new affiliate with email: {}", request.getEmail());
            }


            if (instructorAffiliate.getId()==null && (isAffiliateNicknameExist == null ||
                    isAffiliateNicknameExist.getStatus().equals(GenericStatus.INACTIVE))) {
                // Create new affiliation
                log.info("Creating new affiliation for instructor ID: {} and affiliate ID: {}", instructor.getId(), affiliate==null?1L:affiliate.getId());
                String generatedUuid = UUID.randomUUID().toString();

                instructorAffiliate = InstructorAffiliate.builder()
                        .instructor(instructor)
                        .affiliateUser(affiliate)
                        .createdDate(new Date())
                        .username(request.getName())
                        .defaultReward(request.getDefaultReward())
                        .nickname(request.getNickName())
                        .status(GenericStatus.ACTIVE)
                        .affiliateUuid(generatedUuid)
                        .build();

                instructorAffiliate = instructorAffiliateRepository.save(instructorAffiliate);

                log.info("Affiliate user created successfully with ID: {}", instructorAffiliate.getId());
            }
        String username=instructorAffiliate.getUsername();
        if (isStripeAccExist) {

            Message<String> stripeUrl = this.createAccountLink(affiliate.getEmail());
            // Ensure the data property is parsed as JSON
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // Correct the data format to valid JSON
                String jsonString = stripeUrl.getData()
                        .replace("=", "\":\"")
                        .replace(", ", "\", \"")
                        .replace("{", "{\"")
                        .replace("}", "\"}");

                // Print the corrected JSON string for debugging
                log.info("Formatted JSON string: " + jsonString);

                // Parse the corrected string as JSON
                JsonNode jsonNode = objectMapper.readTree(jsonString);
                log.info("Converted to JsonNode: " + jsonNode);
                // Extract specific keys
                String stripeAccount = jsonNode.get("stripeAccount").asText();
                String accountUrl = jsonNode.get("accountUrl").asText();

                log.info("Stripe Account: " + stripeAccount);
                log.info("Account URL: " + accountUrl);

                if (stripeUrl.getStatus() == 200) {
                    //send email with stripe url
                    Message<String> emailSend = this.sendEmailLinkForStripeUrl(affiliate,username, accountUrl);

                }


            } catch (JsonProcessingException e) {
                e.printStackTrace(); // Handle parsing errors
            }
        }else {
            // send email just for notify add as an affiliate
            Message<String> emailSend = this.sendEmailLinkForStripeUrl(affiliate,username,null);
        }




        return new Message<InstructorAffiliate>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Affiliate user created successfully")
                .setData(instructorAffiliate);
    }

    public void createSelfAffiliate(String email) {
        log.info("Creating affiliate for email: {}", email);

        Optional<User> instructorOpt = userRepository.findByEmail(email);
        if (!instructorOpt.isPresent()) {
            log.error("Affiliate creation failed: Instructor not found with email: {}", email);
            // Return a response or handle the failure appropriately here, if needed
            return;
        }

        log.info("Instructor found: {}", instructorOpt.get().getFullName());

        // Determine payout status
        PayoutStatus payoutStatus = null;
        if (instructorOpt.get().getStripeAccountId() != null) {
            payoutStatus = PayoutStatus.ACTIVATED;
            log.info("Instructor {} has a Stripe account, setting payout status to ACTIVATED", instructorOpt.get().getFullName());
        } else {
            payoutStatus = PayoutStatus.PENDING;
            log.info("Instructor {} does not have a Stripe account, setting payout status to PENDING", instructorOpt.get().getFullName());
        }

        // Check if the affiliate already exists
        Affiliate affiliate = affiliateRepository.findByEmail(email);
        if (affiliate == null) {
            log.info("No existing affiliate found for email: {}", email);
            try {
                affiliate = Affiliate.builder()
                        .email(email)
                        .onboardStatus(payoutStatus)
                        .createdStripeDate(new Date())
                        .build();
                log.info("New affiliate created: {}", affiliate);
                affiliate = affiliateRepository.save(affiliate);
                log.info("Affiliate saved with ID: {}", affiliate.getId());
            } catch (Exception e) {
                log.error("Error while creating new affiliate for email {}: {}", email, e.getMessage());
                return; // Handle or return appropriately
            }
        } else {
            log.info("Affiliate already exists for email: {}", email);
        }

        // Continue only if the affiliate was successfully saved
        if (affiliate.getId() != null) {
            Tuple isSelfAffiliateExist = instructorAffiliateRepository.findByInstructorAndAffiliateId(instructorOpt.get().getId(), affiliate.getId());
            if (isSelfAffiliateExist == null) {

            String nickname = "self";
            try {
                InstructorAffiliate isExistNickname = instructorAffiliateRepository.findByInstructorAndNickName(instructorOpt.get().getId(), nickname);
                if (isExistNickname != null) {
                    log.info("Nickname 'SELF' already exists for instructor: {}", instructorOpt.get().getFullName());

                    // Generate a unique nickname (you can customize this logic as per your requirements)
                    String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
                    nickname = "SELF-" + uniqueSuffix; // Create a unique nickname, e.g., "SELF-d3f8a3d2"
                    log.info("Generated unique nickname: {}", nickname);
                }
                String generatedUuid = UUID.randomUUID().toString();
                InstructorAffiliate instructorAffiliate = InstructorAffiliate.builder()
                        .affiliateUser(affiliate)
                        .instructor(instructorOpt.get())
                        .defaultReward(90.0)
                        .username(instructorOpt.get().getFullName())
                        .nickname(nickname)
                        .createdDate(new Date())
                        .affiliateUuid(generatedUuid)
                        .status(GenericStatus.ACTIVE)
                        .build();

                // Save the instructor affiliate
                instructorAffiliate = instructorAffiliateRepository.save(instructorAffiliate);
                if (instructorAffiliate != null) {
                    log.info("Instructor affiliate created successfully with ID: {}", instructorAffiliate.getId());
                } else {
                    log.error("Failed to create instructor affiliate for instructor: {}", instructorOpt.get().getFullName());
                }
            } catch (Exception e) {
                log.error("Error while creating instructor affiliate for email {}: {}", email, e.getMessage());
            }
        }
        } else {
            log.error("Affiliate creation failed, affiliate ID is null for email: {}", email);
        }
    }
    public Message<String> createAccountLink(String email) throws StripeException{
        log.info("Creating connected account for instructor...");
        Stripe.apiKey = SECRET_KEY;
        Affiliate affiliateUser=affiliateRepository.findByEmail(email);
        AccountCreateParams accountParam = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)  // Use Express for partial control
                .setCountry("US")  // The country of the instructor
                .setEmail(email)  // Instructor's email
                .build();

        Account account = Account.create(accountParam);

        log.info("Account ID: " + account.getId());
        affiliateUser.setStripeAccountId(account.getId());
        affiliateUser.setCreatedStripeDate(new Date());
        affiliateRepository.save(affiliateUser);
        String successRedirectUrl = this.generateStripeRedirectUrl(account.getId(), "ACTIVATED");

        String failedRedirectUrl = this.generateStripeRedirectUrl(account.getId(), "FAILED");

        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(account.getId())  // Connected account ID (instructor's account)
                .setRefreshUrl(failedRedirectUrl)  // Redirect on failure
                .setReturnUrl(successRedirectUrl)  // Redirect on success
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();
        var acc = AccountLink.create(params);

        Map<String,String> response=new HashMap<>();
        response.put("accountUrl",acc.getUrl());
        response.put("stripeAccount",affiliateUser.getStripeAccountId());

        return new Message<String>()
                .setMessage("Successfully connected account redirection link.")
                .setCode(HttpStatus.OK.name())
                .setStatus(HttpStatus.OK.value())
                .setData(response.toString());  // Returns the onboarding link
    }
    public String generateStripeRedirectUrl(String accountId, String status) {
        // Add accountId and status to the URL dynamically
        String finalUrl = stripeRedirectUrlTemplate + "?accountUrl=" + accountId + "&status=" + status;

        return finalUrl;
    }

    public Message<String> sendEmailLinkForStripeUrl(Affiliate affiliate,String username,String stripeUrl) {
        if (affiliate == null) {
            throw new IllegalArgumentException("Affiliate cannot be null");
        }

        log.info("stripe Url "+ stripeUrl==null?"empty":stripeUrl);
        log.info("Send email for stripe Url: ");
        if (stripeUrl!=null) {
            emailService.sendEmail(affiliate.getEmail(), "Welcome to the FastLearner Affiliate Program!",
                    AffiliateUserEmail.createAffiliateWithStripeUrl(username, stripeUrl), true);
        }else {
            emailService.sendEmail(affiliate.getEmail(), "Welcome to the FastLearner Affiliate Program!",
                    AffiliateUserEmail.createAffiliateWithoutStripeUrl(username), true);
        }
        return new Message<String>()
                .setData("Email sent successfully.")
                .setMessage("Email sent successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }

    @Override
    public Message<AffiliationResponseByPaginated> getAffiliateByInstructor(String search,Principal principal, Pageable pageable) {
        log.info("Checking if instructor exists with email: {}", principal.getName());
        Optional<User> instructorOpt = userRepository.findByEmail(principal.getName());
        if (!instructorOpt.isPresent()) {
            log.info("Instructor not found with email: {}", principal.getName());
            return new Message<AffiliationResponseByPaginated>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor not found with email: " + principal.getName());
        }
        User instructor = instructorOpt.get();
        log.info("Instructor found with ID: {}", instructor.getId());
        // Retrieve affiliations by instructor
        Page<Tuple> affiliations = instructorAffiliateRepository.findByInstructorAndIsActive(search,instructor.getId(),instructor.getEmail(),pageable);
        if (affiliations==null||affiliations.isEmpty()) {
            log.info("No affiliates found for instructor ID: {}", instructor.getFullName());
            return new Message<AffiliationResponseByPaginated>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("No affiliates found for instructor ID: " + instructor.getFullName());
        }
        List<AffiliationResponse> affiliationList=affiliations.stream().map(c->{
            AffiliationResponse affiliationResponse=AffiliationResponse.builder()
                    .affiliateId((Long) c.get("id"))
                    .instructorAffiliateId((Long) c.get("affiliation_id"))
                    .email((String) c.get("email"))
                    .name((String) c.get("username"))
                    .nickName((String) c.get("nick_name"))
                    .defaultReward((Double) c.get("default_reward"))
                    .isSelf((Boolean) c.get("is_self"))
                    .uuid((String) c.get("affiliate_uuid"))
                    .build();
            return affiliationResponse;
        }).collect(Collectors.toList());



        log.info("Found {} active affiliates for instructor ID: {}", affiliations.getTotalElements(), instructor.getFullName());


        return new Message<AffiliationResponseByPaginated>()
                .setData(AffiliationResponseByPaginated.builder()
                        .affiliates(affiliationList)
                        .pageNo(pageable.getPageNumber())
                        .pageSize(pageable.getPageSize())
                        .pages(affiliations.getTotalPages())
                        .totalElements(affiliations.getTotalElements())
                        .build()
                )
                .setMessage("Affiliates retrieved successfully")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());

    }

    @Override
    public Message deleteAffiliateByInstructor(Long affiliateId) {
        log.info("Attempting to delete affiliate with ID: {}", affiliateId);
        if (affiliateId == null) {
            log.info("Affiliate ID is null, cannot delete affiliate");
            return new Message()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Affiliate not found with ID: null");
        }

        log.info("Attempting to delete affiliate with ID: {}", affiliateId);
        Optional<InstructorAffiliate> affiliationOpt = instructorAffiliateRepository.findById(affiliateId);

        if (affiliationOpt.isPresent()) {
            InstructorAffiliate instructorAffiliate = affiliationOpt.get();
            instructorAffiliate.setStatus(GenericStatus.INACTIVE);

            instructorAffiliateRepository.save(instructorAffiliate);

            log.info("Affiliate with ID: {} marked as inactive successfully", affiliateId);

            return new Message()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString())
                    .setMessage("Affiliate deleted successfully");
        } else {
            log.info("Affiliate with ID: {} not found", affiliateId);

            return new Message()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Affiliate not found with ID: " + affiliateId);
        }
    }

    @Override
    public Message updateAffiliateUser(CreateAffiliateReq request, Principal principal) {
        log.info("Starting update process for affiliate user with request: {}", request);

        log.info("Checking if instructor exists with email: {}", principal.getName());
        Optional<User> instructorOpt = userRepository.findByEmail(principal.getName());
        if (!instructorOpt.isPresent()) {
            log.warn("Instructor not found with email: {}", principal.getName());
            return new Message<AffiliationResponseByPaginated>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor not found with email: " + principal.getName());
        }
        User instructor = instructorOpt.get();
        log.info("Instructor found with ID: {}", instructor.getId());

        // Check if the affiliate user already exists with the given nickname
        log.info("Checking if affiliate user exists with nickname: {} for instructor ID: {}",
                request.getNickName(), instructor.getId());
        Optional<InstructorAffiliate> optionalInstructorAffiliate =
                Optional.ofNullable(instructorAffiliateRepository.findByInstructorAndNickNameAndNotAffUserId(request.getInstructorAffiliateId(), instructor.getId(), request.getNickName()));
        if (optionalInstructorAffiliate.isPresent()) {
            log.warn("Affiliate user with nickname '{}' already exists for instructor ID: {}",
                    request.getNickName(), instructor.getId());
            return new Message<InstructorAffiliate>()
                    .setStatus(HttpStatus.CONFLICT.value())
                    .setCode(HttpStatus.CONFLICT.toString())
                    .setMessage("Affiliate user already exists with the given nickname");
        }

        // Fetch the instructor affiliate by ID
        log.info("Fetching Instructor Affiliate with ID: {}", request.getInstructorAffiliateId());
        Optional<InstructorAffiliate> instructorAffiliateOpt =
                instructorAffiliateRepository.findById(request.getInstructorAffiliateId());
        if (!instructorAffiliateOpt.isPresent()) {
            log.warn("Instructor Affiliate not found with ID: {}", request.getInstructorAffiliateId());
            return new Message<InstructorAffiliate>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor Affiliate not found with ID: " + request.getInstructorAffiliateId());
        }
        InstructorAffiliate instructorAffiliate = instructorAffiliateOpt.get();

        // Update the instructor affiliate details
        log.info("Updating Instructor Affiliate details for ID: {}", instructorAffiliate.getId());
        instructorAffiliate.setNickname(request.getNickName());
        instructorAffiliate.setUsername(request.getName());
        instructorAffiliate.setDefaultReward(request.getDefaultReward());
        instructorAffiliate = instructorAffiliateRepository.save(instructorAffiliate);

        // Verify if the update was successful
        if (instructorAffiliate == null) {
            log.error("Failed to update Instructor Affiliate with ID: {}", request.getInstructorAffiliateId());
            return new Message()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                    .setMessage("Failed to update Affiliate user");
        }

        log.info("Successfully updated Affiliate user with ID: {}", instructorAffiliate.getId());
        return new Message()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Affiliate user updated successfully");
    }

    @Override
    public Message getAffiliateUserByAffiliateId(Long instructorAffiliateId, Principal principal) {
        log.info("Request received to fetch affiliate details for instructor affiliate ID: {}", instructorAffiliateId);
        log.info("Checking if instructor exists with email: {}", principal.getName());

        Optional<User> instructorOpt = userRepository.findByEmail(principal.getName());
        if (!instructorOpt.isPresent()) {
            log.warn("Instructor not found with email: {}", principal.getName());
            return new Message<AffiliationResponseByPaginated>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor not found with email: " + principal.getName());
        }

        User instructor = instructorOpt.get();
        log.info("Instructor found with ID: {}, Name: {}", instructor.getId(), instructor.getFullName());

        log.info("Checking if affiliate exists for instructor affiliate ID: {}", instructorAffiliateId);
        Tuple instructorAffiliate = instructorAffiliateRepository.findByInstructorAff(instructorAffiliateId,instructor.getEmail());
        if (instructorAffiliate == null) {
            log.warn("No affiliates found for instructor affiliate ID: {}", instructorAffiliateId);
            return new Message<AffiliationResponseByPaginated>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("No affiliates found for instructor affiliate ID: " + instructorAffiliateId);
        }
        Object onboardedStudentsObj = instructorAffiliate.get("onboarded_students");
        Long onboardedStudents = onboardedStudentsObj instanceof Number
                ? ((Number) onboardedStudentsObj).longValue()
                : null;

        log.info("Affiliate details retrieved successfully for ID: {}", instructorAffiliateId);
        AffiliateDetailResponse affiliationResponse = AffiliateDetailResponse.builder()
                .affiliateId((Long) instructorAffiliate.get("affiliate_id"))
                .instructorAffiliateId((Long) instructorAffiliate.get("instructor_affiliate_id"))
                .email((String) instructorAffiliate.get("email"))
                .name((String) instructorAffiliate.get("username"))
                .nickName((String) instructorAffiliate.get("nick_name"))
                .rewards((Double) instructorAffiliate.get("default_reward"))
                .totalRevenue((Double) instructorAffiliate.get("total_revenue"))
                .onboardStatus((String) instructorAffiliate.get("onboard_status"))
                .totalOnboardedStudent(onboardedStudents)
                .isSelf((Boolean) instructorAffiliate.get("is_self"))
                .build();

        log.info("AffiliationResponse successfully constructed for instructor affiliate ID: {}", instructorAffiliateId);

        return new Message<AffiliateDetailResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Affiliate detail fetched successfully")
                .setData(affiliationResponse);
    }

    @Override
    public Message stripeResendLinkForAffiliate(String email, Principal principal) throws StripeException, JsonProcessingException {
        Optional<User> instructorOpt = userRepository.findByEmail(principal.getName());
        if (!instructorOpt.isPresent()) {
            log.warn("Instructor not found with email: {}", principal.getName());
            return new Message<AffiliationResponseByPaginated>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor not found with email: " + principal.getName());
        }

        User instructor = instructorOpt.get();
        log.info("Instructor found with ID: {}, Name: {}", instructor.getId(), instructor.getFullName());

        Affiliate affiliate = affiliateRepository.findByEmail(email);
        if (affiliate != null) {
            Tuple instructorAffiliate=instructorAffiliateRepository.findByInstructorAndAffiliateId(instructor.getId(), affiliate.getId() );
            Date createdStripeDate = affiliate.getCreatedStripeDate();
            if (TimeUtil.isWithinOneDay(createdStripeDate)) {
                Message<String> emailSend = this.sendEmailLinkForStripeUrl(affiliate, (String) instructorAffiliate.get("username"),null);
            } else {
                Message<String> stripeUrl = this.createAccountLink(affiliate.getEmail());
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    // Correct the data format to valid JSON
                    String jsonString = stripeUrl.getData()
                            .replace("=", "\":\"")
                            .replace(", ", "\", \"")
                            .replace("{", "{\"")
                            .replace("}", "\"}");

                    // Print the corrected JSON string for debugging
                    log.info("Formatted JSON string: " + jsonString);

                    // Parse the corrected string as JSON
                    JsonNode jsonNode = objectMapper.readTree(jsonString);
                    log.info("Converted to JsonNode: " + jsonNode);
                    // Extract specific keys
                    String stripeAccount = jsonNode.get("stripeAccount").asText();
                    String accountUrl = jsonNode.get("accountUrl").asText();

                    log.info("Stripe Account: " + stripeAccount);
                    log.info("Account URL: " + accountUrl);

                    if (stripeUrl.getStatus() == 200) {
                        //send email with stripe url
                        Message<String> emailSend = this.sendEmailLinkForStripeUrl(affiliate,(String) instructorAffiliate.get("username"), accountUrl);
                    }
                    }catch (Exception e){
                    log.info("Exception:" +e.getMessage());
                }
            }
        }
        return new Message<AffiliateDetailResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Stripe Link send Successfully");
    }

    @Override
    public Affiliate findById(Long id) throws EntityNotFoundException {
        return this.affiliateRepository.findById(id).orElseThrow(() -> {
            log.error("User not found.");
            return new EntityNotFoundException("Affiliate not found.");
        });
    }


    @Override
    public Message stripeRedirectUrlForAffiliate(String accountId, String status) {
        Affiliate affiliate =affiliateRepository.findByStripeAccountId(accountId);
        if (affiliate!=null){
            if (!status.isEmpty()) {
                if (status.equalsIgnoreCase(String.valueOf(PayoutStatus.ACTIVATED))) {
                    affiliate.setOnboardStatus(PayoutStatus.ACTIVATED);
                }
                else {
                    affiliate.setOnboardStatus(PayoutStatus.PENDING);
                }
            }else {
                affiliate.setOnboardStatus(PayoutStatus.FAILED);
            }
        }
        affiliateRepository.save(affiliate);
        return new Message<AffiliateDetailResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Affiliate status saved successfully");
    }

}
