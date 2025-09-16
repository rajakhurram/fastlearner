package com.vinncorp.fast_learner.services.course;

import com.vinncorp.fast_learner.dtos.course.CourseDetailForCertificate;
import com.vinncorp.fast_learner.dtos.course.CourseDropdown;
import com.vinncorp.fast_learner.dtos.course.CourseUrlDto;
import com.vinncorp.fast_learner.dtos.course.RelatedCourses;
import com.vinncorp.fast_learner.dtos.section.SectionDetail;
import com.vinncorp.fast_learner.dtos.section.TopicDetail;
import com.vinncorp.fast_learner.dtos.topic.NoOfTopicInCourse;
import com.vinncorp.fast_learner.es_dto.CourseContentResponse;
import com.vinncorp.fast_learner.es_dto.ScoredItem;
import com.vinncorp.fast_learner.es_dto.SectionContentResponse;
import com.vinncorp.fast_learner.es_dto.TopicContentResponse;
import com.vinncorp.fast_learner.es_models.CourseContent;
import com.vinncorp.fast_learner.es_models.SectionContent;
import com.vinncorp.fast_learner.es_models.TopicContent;
import com.vinncorp.fast_learner.es_services.IESCourseService;
import com.vinncorp.fast_learner.es_services.course_content.IESCourseContentService;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.CreateCourseValidationException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.article.Article;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseCategory;
import com.vinncorp.fast_learner.models.course.CourseLevel;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.quiz.Quiz;
import com.vinncorp.fast_learner.models.quiz.QuizQuestion;
import com.vinncorp.fast_learner.models.quiz.QuizQuestionAnwser;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.SubscriptionValidations;
import com.vinncorp.fast_learner.models.tag.Tag;
import com.vinncorp.fast_learner.models.topic.Topic;
import com.vinncorp.fast_learner.models.topic.TopicType;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.models.user.UserProfile;
import com.vinncorp.fast_learner.models.video.Video;
import com.vinncorp.fast_learner.repositories.api_client.ApiClientRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.request.docs.CreateDocumentsRequest;
import com.vinncorp.fast_learner.request.quiz.CreateQuizQuestionAnswerRequest;
import com.vinncorp.fast_learner.request.quiz.CreateQuizQuestionRequest;
import com.vinncorp.fast_learner.request.section.CreateSectionRequest;
import com.vinncorp.fast_learner.request.tag.CreateTagRequest;
import com.vinncorp.fast_learner.request.topic.CreateTopicRequest;
import com.vinncorp.fast_learner.response.course.*;
import com.vinncorp.fast_learner.response.course.nlp_search.CourseResponse;
import com.vinncorp.fast_learner.response.subscriptionpermission.SubscriptionPermissionResponse;
import com.vinncorp.fast_learner.services.affiliate.affiliate_service.AffiliateService;
import com.vinncorp.fast_learner.services.article.IArticleService;
import com.vinncorp.fast_learner.services.course.course_review.ICourseReviewService;
import com.vinncorp.fast_learner.services.docs.IDocumentService;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.quiz.IQuizQuestionAnswerService;
import com.vinncorp.fast_learner.services.quiz.IQuizQuestionService;
import com.vinncorp.fast_learner.services.quiz.IQuizService;
import com.vinncorp.fast_learner.services.section.ISectionService;
import com.vinncorp.fast_learner.services.subscription.ISubscribedUserService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionValidationsService;
import com.vinncorp.fast_learner.services.tag.ITagService;
import com.vinncorp.fast_learner.services.topic.ITopicService;
import com.vinncorp.fast_learner.services.topic.ITopicTypeService;
import com.vinncorp.fast_learner.services.user.IUserProfileService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.services.video.IVideoService;
import com.vinncorp.fast_learner.models.docs.Document;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.repositories.course.CourseRepository;
import com.vinncorp.fast_learner.request.course.CourseByCategoryRequest;
import com.vinncorp.fast_learner.request.course.CreateCourseRequest;
import com.vinncorp.fast_learner.request.course.RelatedCoursesRequest;
import com.vinncorp.fast_learner.request.course.SearchCourseRequest;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.*;
import com.vinncorp.fast_learner.util.exception.ExceptionUtils;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CourseService implements ICourseService {

    @Value("${transcript.generation.auth-key}")
    private String AUTH_TOKEN;

    @Value("${related.courses.api}")
    private String RELATED_COURSE_PYTHON_SERVICE;

    @Value("${search.related.courses.api}")
    private String NLP_SEARCH_PYTHON_SERVICE;

    private final CourseRepository repo;

    private final ICourseLevelService courseLevelService;
    private final ICourseCategoryService courseCategoryService;
    private final IUserService userService;
    private final ISectionService sectionService;
    private final ITopicTypeService topicTypeService;
    private final ITopicService topicService;
    private final IQuizService quizService;
    private final IQuizQuestionService quizQuestionService;
    private final IQuizQuestionAnswerService quizQuestionAnswerService;
    private final IVideoService videoService;
    private final ITagService tagService;
    private final ICourseReviewService courseReviewService;
    private final RestTemplate restTemplate;
    private final IArticleService articleService;
    private final IESCourseService esCourseService;
    private final IDocumentService documentService;
    private final ICourseVisitorService courseVisitorService;
    private final RabbitMQProducer rabbitMQProducer;
    private final IEnrollmentService enrollmentService;
    private final ICourseUrlService courseUrlService;
    private final IESCourseContentService esCourseContentService;
    private final IUserProfileService userProfileService;
    private final ApiClientRepository apiClientRepository;

    private final AffiliateService affiliateService;
    private final UserRepository userRepository;

    private final ISubscribedUserService subscribedUserService;

    private final ISubscriptionValidationsService subscriptionValidationsService;

    public CourseService(CourseRepository repo, ICourseLevelService courseLevelService,
                         ICourseCategoryService courseCategoryService, IUserService userService,
                         ISectionService sectionService, ITopicTypeService topicTypeService,
                         ITopicService topicService, IQuizService quizService, IQuizQuestionService quizQuestionService,
                         IQuizQuestionAnswerService quizQuestionAnswerService, IVideoService videoService,
                         ITagService tagService, @Lazy ICourseReviewService courseReviewService,
                         RestTemplate restTemplate, IArticleService articleService, IESCourseService esCourseService,
                         IDocumentService documentService, ICourseVisitorService courseVisitorService, RabbitMQProducer rabbitMQProducer,
                         IEnrollmentService enrollmentService, ICourseUrlService courseUrlService, IESCourseContentService esCourseContentService,
                         IUserProfileService userProfileService, ApiClientRepository apiClientRepository, AffiliateService affiliateService, UserRepository userRepository, ISubscribedUserService subscribedUserService, ISubscriptionValidationsService subscriptionValidationsService) {

        this.repo = repo;
        this.courseLevelService = courseLevelService;
        this.courseCategoryService = courseCategoryService;
        this.userService = userService;
        this.sectionService = sectionService;
        this.topicTypeService = topicTypeService;
        this.topicService = topicService;
        this.quizService = quizService;
        this.quizQuestionService = quizQuestionService;
        this.quizQuestionAnswerService = quizQuestionAnswerService;
        this.videoService = videoService;
        this.tagService = tagService;
        this.courseReviewService = courseReviewService;
        this.restTemplate = restTemplate;
        this.articleService = articleService;
        this.esCourseService = esCourseService;
        this.documentService = documentService;
        this.courseVisitorService = courseVisitorService;
        this.rabbitMQProducer = rabbitMQProducer;
        this.enrollmentService = enrollmentService;
        this.courseUrlService = courseUrlService;
        this.esCourseContentService = esCourseContentService;
        this.userProfileService = userProfileService;
        this.apiClientRepository = apiClientRepository;
        this.affiliateService = affiliateService;
        this.userRepository = userRepository;
        this.subscribedUserService = subscribedUserService;
        this.subscriptionValidationsService = subscriptionValidationsService;
    }


    public void createCourseValidateForThirdParty(CreateCourseRequest request) throws CreateCourseValidationException, EntityNotFoundException {
        Map<String, String> error = new HashMap<>();

        if (request.getCourseType() != null) {
            if (request.getCourseType().equalsIgnoreCase(String.valueOf(CourseType.PREMIUM_COURSE))) {
                if (request.getPrice() == null || request.getPrice() <= 0){
                    error.put("price", "The course price must be valid and greater than zero.");
                }
            }else if (request.getCourseType().equalsIgnoreCase(String.valueOf(CourseType.STANDARD_COURSE))){
                if (request.getPrice() != null){
                    error.put("price", "Standard courses cannot have a price.");
                }
            }else{
                error.put("courseType", "Course Type is not supported.");
            }
        } else {
            error.put("courseType", "A valid course type is required.");
        }

        if (!validateAlphanumericString(request.getTitle())){
            error.put("title", "Course title must be valid and between 10 to 60 characters.");
        }

        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            error.put("description", "Description is required and cannot be empty.");
        } else if (request.getDescription().length() > 3000) {
            error.put("description", "Description must not exceed 3000 characters.");
        }

        try{
            if (request.getCategoryId() == null || courseCategoryService.findById(request.getCategoryId()) == null) {
                error.put("categoryId",  "Category ID is required and must be valid.");
            }
        }catch (EntityNotFoundException e){
            error.put("categoryId",  "Category ID is required and must be valid.");
        }

        try{
            if (request.getCourseLevelId() == null || courseLevelService.findById(request.getCourseLevelId()) == null) {
                error.put("courseLevelId",  "Course Level ID is required and must be valid.");
            }
        }catch (EntityNotFoundException e){
            error.put("courseLevelId",  "Course Level ID is required and must be valid.");
        }

        if (request.getAbout() == null) {
            error.put("about", "About is required");
        } else if (request.getAbout().length() >= 300) {
            error.put("about", "About must not exceed 300 characters.");
        }
        if (request.getThumbnailUrl() == null) {
            error.put("thumbnailUrl", "Thumbnail url is required");
        }
        if (request.getPreviewVideoURL() == null) {
            error.put("previewVideoURL", "Preview video url is required");
        }
        if (request.getPrerequisite() == null || request.getPrerequisite().isEmpty()) {
            error.put("prerequisite", "Prerequisite is required and cannot be empty.");
        } else {
            boolean hasInvalidLength = request.getPrerequisite().stream()
                    .anyMatch(c -> c == null || c.length() > 500);

            if (hasInvalidLength) {
                error.put("prerequisite", "Each prerequisite must be a non-null string with a maximum length of 500 characters.");
            }
        }
        if (request.getCourseOutcomes() != null && request.getCourseOutcomes().size() > 0) {
            boolean hasInvalidCourseOutcome = request.getCourseOutcomes().stream().anyMatch(s -> s == null || s.length() > 500);
            if (hasInvalidCourseOutcome) {
                error.put("courseOutcomes", "Each course outcome must be a non-null string with a maximum length of 500 characters.");
            }
        } else {
            error.put("courseOutcomes", "Course Outcome size must be greater than zero");
        }


        if (request.getSections() != null && request.getSections().size() > 0) {
            Boolean isSectionFree = true;
            for (CreateSectionRequest sections : request.getSections()) {

                sections.setDelete(false);

                if (sections.getTitle() == null) {
                    error.put("title", "Section title is required.");
                } else if (sections.getTitle().length() > 100) {
                    error.put("title", "Section title cannot exceed 100 characters.");
                }
                if (sections.getLevel() <= 0) {
                    error.put("level", "Section level must be a positive number.");
                }
                if (isSectionFree) {
                    sections.setIsFree(true);
                    isSectionFree = false;
                } else {
                    sections.setIsFree(false);
                }
                // Validate topics
                if (sections.getTopics() != null && !sections.getTopics().isEmpty()) {
                    for (CreateTopicRequest topic : sections.getTopics()) {
                        topic.setDelete(false);

                        if (topic.getLevel() <= 0) {
                            error.put("level", "Topic level must be a positive number.");
                        }

                        if (topic.getTitle() == null) {
                            error.put("title", "Topic title is required.");
                        } else if (topic.getTitle().length() > 100) {
                            error.put("title", "Topic title cannot exceed 100 characters.");
                        }
                        if (topic.getTopicTypeId() == null) {
                            error.put("topicTypeId", "Topic type id must be required");
                        }

                        if (topic.getTopicTypeId() == 1) {
                            if (topic.getDuration() < 0) {
                                error.put("duration", "Topic duration must be greater than zero");
                            }
                            if (topic.getVideo() != null) {
                                topic.getVideo().setDelete(false);
                                if (topic.getVideo().getFilename() == null) {
                                    error.put("video", "Video filename must be required");
                                }
                                if (topic.getVideo().getVideoURL() == null) {
                                    error.put("videoURL", "Topic Video url must be required");
                                }
                                if (topic.getVideo().getDocuments() != null && !topic.getVideo().getDocuments().isEmpty()) {
                                    for (CreateDocumentsRequest docs : topic.getVideo().getDocuments()) {
                                        docs.setDelete(false);
                                        if (docs.getDocName() == null || docs.getDocName().trim().isEmpty()) {
                                            error.put("docName", "Document name is required and cannot be blank.");
                                        }
                                        if (docs.getDocUrl() == null || docs.getDocUrl().trim().isEmpty()) {
                                            error.put("docUrl", "Document url is required and cannot be blank.");
                                        }
                                        if (docs.getSummary() == null || docs.getSummary().trim().isEmpty()) {
                                            error.put("summary", "Document summary is required and cannot be blank.");
                                        }
                                    }
                                }


                            } else {
                                error.put("video", "Topic Video must be required");
                            }
                        } else if (topic.getTopicTypeId() == 2) {
                            if (topic.getArticle() != null) {
                                if (topic.getArticle().getArticle() == null || topic.getArticle().getArticle().trim().isEmpty()) {
                                    error.put("article", "Topic article is required.");
                                }
                                //discuss
                                if (topic.getArticle().getDocuments() != null) {
                                    if (topic.getArticle().getDocuments().size() > 0) {
                                        if (topic.getArticle().getDocuments().size() < 2) {
                                            for (CreateDocumentsRequest document : topic.getArticle().getDocuments()) {
                                                document.setDelete(false);
                                                if (document.getSummary() == null || document.getSummary().trim().isEmpty()) {
                                                    error.put("summary", "Document summary is required and cannot be blank.");
                                                }
                                                if (document.getDocName() == null || document.getDocName().trim().isEmpty()) {
                                                    error.put("docName", "Document name is required and cannot be blank.");
                                                }
                                                if (document.getDocUrl() == null || document.getDocUrl().trim().isEmpty()) {
                                                    error.put("docUrl", "Document url is required and cannot be blank.");
                                                }
                                            }
                                        } else {
                                            error.put("documents", "Topic article documents not greater than one array");
                                        }
                                    }
                                }
                            }

                        } else if (topic.getTopicTypeId() == 4) {
                            if (topic.getQuiz() != null) {
                                topic.getQuiz().setDelete(false);
                                if (topic.getQuiz().getTitle() == null || topic.getQuiz().getTitle().trim().isEmpty()) {
                                    error.put("title", "Quiz title must be required");
                                }
                                if (topic.getQuiz().getQuestions().size() > 0) {
                                    for (CreateQuizQuestionRequest quizQuestion : topic.getQuiz().getQuestions()) {
                                        quizQuestion.setDelete(false);

                                        if (quizQuestion.getQuestionText() == null || quizQuestion.getQuestionText().trim().isEmpty()) {
                                            error.put("questionText", "Question Text must be required");
                                        }
                                        if (quizQuestion.getAnswers().size() > 1) {
                                            Boolean isAnswerExist = false;
                                            for (CreateQuizQuestionAnswerRequest quizQuestionAnswer : quizQuestion.getAnswers()) {
                                                quizQuestionAnswer.setDelete(false);

                                                if (quizQuestionAnswer.getAnswerText() == null || quizQuestionAnswer.getAnswerText().trim().isEmpty()) {
                                                    error.put("answerText", "Answer text must be required");
                                                }
                                                if (quizQuestionAnswer.getIsCorrectAnswer() == null) {
                                                    error.put("isCorrectAnswer", "Correct answer must be required");
                                                }
                                                if (quizQuestionAnswer.getIsCorrectAnswer()) {
                                                    isAnswerExist = true;
                                                }

                                            }
                                            if (!isAnswerExist) {
                                                error.put("isCorrectAnswer", "Each question have one correct answer required");
                                            }
                                        } else {
                                            error.put("answers", "Each question must be required a two answers");
                                        }

                                    }
                                } else {
                                    error.put("questions", "Each quiz have one Question required");
                                }
                            } else {
                                error.put("quiz", "Topic Quiz must be required");
                            }
                        } else {
                            error.put("topicTypeId", "Topic type id did not match");
                        }
                    }
                } else {
                    error.put("topics", "Each section must have at least one topic.");
                }

            }
        } else {
            error.put("sections", "At least one section is required.");
        }
        if (request.getTags() != null && request.getTags().size() > 0) {
            for (CreateTagRequest tags : request.getTags()) {
                tags.setActive(true);
                if (tags.getName() == null || tags.getName().trim().isEmpty() || tags.getName().length() >= 50) {
                    error.put("name", "Tags name must be required or not greater than fifty character");
                }
            }
        } else {
            error.put("tags", "Tags must be required");
        }

        request.setIsActive(true);
        request.setCertificateEnabled(true);


        if (error.size() > 0) {
            throw new CreateCourseValidationException("Some validation error occurred.", error);  // Throwing the exception

        }
    }

    /*This method ensures that your string contains a mix of letters, numbers, and special characters,
    but is not entirely numbers or special characters.
    */
    private boolean validateAlphanumericString(String title) {
        String regex = "^(?=.*[a-zA-Z])[a-zA-Z0-9!@#$%^&*()_+={}\\[\\]:;\"'<>,.?/\\\\|`~\\-]{10,60}$\n";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(title);
        return matcher.matches();
    }

    // Below method is used to check create premium course validation by which subscription buy
    public boolean premiumCourseValidationBySubscription(User user) throws EntityNotFoundException {

            log.info("Instructor found with ID: {}", user.getId());

            SubscribedUser subscribedUser = subscribedUserService.findByUser(user.getEmail());

            if (subscribedUser != null && subscribedUser.getSubscription().getId()!=1) {
                Long subscriptionId = (subscribedUser.getSubscription() != null) ?
                        subscribedUser.getSubscription().getId() : null;

                log.info("Checking subscription validations for subscription ID: {}", subscriptionId);
                SubscriptionValidations subscriptionValidations = subscriptionValidationsService
                        .findByValidationNameAndSubscriptionAndIsActive("PREMIUM_COURSE", subscribedUser.getSubscription(), true);

                if (subscriptionValidations == null) {
                    log.warn("No subscription validations found for PREMIUM_COURSE with subscription ID: {}", subscriptionId);
                    return false;
                }

                Long countCourse = repo.findByInstructorAndPublished(user.getId(), "PUBLISHED");
                log.info("Total published courses for instructor ID {}: {}", user.getId(), countCourse);

                Long isAvailableCourse = subscriptionValidations.getValue() - countCourse;
                log.info("Remaining premium courses available: {}", isAvailableCourse);

                if (isAvailableCourse>0){
                    return true;
                }else {
                   return false;
                }
            } else {
                log.warn("Free subscription found for user with email: {}", user.getEmail());
               return false;
            }
        }

    @Transactional
    @Override
    public Message<CreateCourseRequest> createCourse(CreateCourseRequest request, String clientId, String email)
            throws EntityNotFoundException, InternalServerException, BadRequestException, IOException, CreateCourseValidationException {
        // If the request is coming from third party app.
        if (clientId != null) {
            this.createCourseValidateForThirdParty(request);
        }

        boolean isCreation = false;
        if (request.getIsActive()) {
            this.validateCreateCourseData(request);
        }
        if (Objects.isNull(request.getCourseId())) {
            isCreation = true;
        } else {
            var savedCourse = repo.findById(request.getCourseId());
            if (savedCourse.isEmpty())
                throw new BadRequestException("Course id provided is not valid.");

            if (request.getIsActive() && savedCourse.get().getCourseType() == CourseType.PREMIUM_COURSE &&
                    CourseType.PREMIUM_COURSE != CourseType.valueOf(request.getCourseType())) {
                throw new BadRequestException("Course type cannot be changed after publishing the course.");
            }

        }



        log.info("Creating course for user: " + email);
        User instructor = userService.findByEmail(email);
        if (!isCreation && request.getCourseType().equalsIgnoreCase(String.valueOf(CourseType.PREMIUM_COURSE))){
            SubscribedUser subscribedUser = subscribedUserService.findByUser(instructor.getEmail());
            List<PlanType> planType = Arrays.asList(PlanType.STANDARD, PlanType.FREE);
            if (subscribedUser != null && planType.contains(subscribedUser.getSubscription().getPlanType())) {
                log.warn("Edit attempt blocked for user: {} due to subscription plan: {}", instructor.getEmail(), subscribedUser.getSubscription().getPlanType());
                throw new BadRequestException("You are not allowed to edit the course because you are subscribed to the "
                        + subscribedUser.getSubscription().getPlanType() + " plan.");
            }
        }else if (request.getCourseType().equalsIgnoreCase(String.valueOf(CourseType.PREMIUM_COURSE))) {
                log.info("Request received for creating a course of type: PREMIUM_COURSE for instructor: {}", instructor.getId());

                boolean isPremiumCourseAvailable = this.premiumCourseValidationBySubscription(instructor);
                if (!isPremiumCourseAvailable) {
                    log.error("Premium course creation failed for instructor: {}. Reason: Premium course subscription validation failed.", instructor.getId());
                    throw new BadRequestException("Premium course cannot be created because subscription validation failed.");
                }
            }

        CourseLevel courseLevel = Objects.isNull(request.getCourseLevelId()) ? null : courseLevelService.findById(request.getCourseLevelId());
        CourseCategory courseCategory = Objects.isNull(request.getCategoryId()) ? null : courseCategoryService.findById(request.getCategoryId());

        Course course = savingCourse(request, instructor, courseLevel, courseCategory);
        tagService.createAllNewAndAlreadyExistsTags(request.getTags(), course);
        List<SectionDetail> savedSections = null;
        if (!CollectionUtils.isEmpty(request.getSections())) {
            savedSections = savingSectionAndTopics(request, course);
        }

        if (course.getCourseStatus().equals(CourseStatus.PUBLISHED) && !this.enrollmentService.isEnrolled(course.getId(), email)) {
            this.enrollmentService.enrolled(course.getId(), email, false);
        }

        if (request.getIsActive()) {
            saveESCourseContent(course, request.getTags(), savedSections, instructor);
        }

        request.setCategoryName(Objects.isNull(courseCategory) ? null : courseCategory.getName());
        request.setCourseLevelName(Objects.isNull(courseLevel) ? null : courseLevel.getName());
        request.setCourseId(course.getId());
        if (Objects.isNull(course.getCourseProgress())) {
            course.setCourseProgress("");
        }
        if (course.getCourseProgress().equalsIgnoreCase("100")) {
            CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
            if (Objects.isNull(request.getCourseId())) {
                rabbitMQProducer.sendMessageToUsers(course.getTitle(), course.getCreatedBy(),
                        "/student/course-details/" + courseUrl.getUrl(), course.getContentType(), NotificationContentType.TEXT,
                        NotificationType.NEW_COURSE,
                        course.getId());
            } else {
                rabbitMQProducer.sendMessageToUsers(course.getTitle(), course.getCreatedBy(),
                        "/student/course-details/" + courseUrl.getUrl(), course.getContentType(), NotificationContentType.TEXT,
                        NotificationType.COURSE_UPDATED,
                        course.getId());
            }
        }
        log.info("creating a self affiliate");
        if (course.getCourseType()== CourseType.PREMIUM_COURSE && course.getCourseStatus()==CourseStatus.PUBLISHED) {
            affiliateService.createSelfAffiliate(email);
        }
        String msg = null;
        if (isCreation)
            msg = "Course created successfully.";
        else
            msg = "Course updated successfully.";
        log.info(msg);

        return new Message<CreateCourseRequest>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage(msg)
                .setData(request);
    }

    public Message<String> dumbDbCourseToEs() throws InternalServerException {
        log.info("Dumping the courses into elasticsearch.");
        try {
            List<Course> courses = this.findAllByStatuses(List.of(CourseStatus.PUBLISHED, CourseStatus.UNPUBLISHED));
            for (Course course : courses) {
                List<Tag> tags = this.tagService.findByCourseId(course.getId());
                List<SectionDetail> sectionDetails = new ArrayList<>();
                List<Section> sections = this.sectionService.getAllSectionsByCourseId(course.getId());
                if(!sections.isEmpty()){
                    for (Section s : sections) {
                        List<TopicDetail> topicDetails = new ArrayList<>();
                        SectionDetail sectionDetail = SectionDetail.toSectionDetail(s);
                        List<Topic> topics;
                        try {
                            topics = this.topicService.fetchAllTopicsBySectionId(s.getId());
                        }catch(Exception e){
                            log.error(e.getMessage());
                            continue;
                        }
                        for (Topic t: topics) {
                            TopicDetail topicDetail = TopicDetail.toTopicDetails(t);
                            if(t.getTopicType().getName().equalsIgnoreCase("VIDEO")){
                                try {
                                    topicDetail.setVideoTranscribe(this.videoService.getVideoByTopicId(t.getId()).getTranscribe());
                                }catch(Exception e){
                                    log.error(e.getMessage());
                                    continue;
                                }
                            }else if(t.getTopicType().getName().equalsIgnoreCase("ARTICLE")){
                                try{
                                    topicDetail.setArticle(this.articleService.findByTopicId(t.getId()).getContent());
                                }catch(Exception e){
                                    log.error(e.getMessage());
                                    continue;
                                }
                            }
                            topicDetails.add(topicDetail);
                        }
                        sectionDetail.setTopicDetails(topicDetails);
                        sectionDetails.add(sectionDetail);
                    }
                    this.saveESCourseContent(course, CreateTagRequest.toCreateTagRequest(tags), sectionDetails, this.userService.findById(course.getCreatedBy()));
                }
            }
        } catch (Exception e) {
            throw new InternalServerException(InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
        Message<String> message = new Message<>();
        message.setCode(HttpStatus.OK.toString());
        message.setMessage("Courses dumbed into es index successfully.");
        message.setStatus(HttpStatus.OK.value());
        message.setData("Courses dumbed into es index successfully.");
        return message;
    }

    void saveESCourseContent(Course course, List<CreateTagRequest> tags, List<SectionDetail> sections, User user) throws IOException, EntityNotFoundException {
        UserProfile userProfile = this.userProfileService.getUserProfile(user.getId());
        CourseContent courseContent = new CourseContent();
        courseContent.setId(course.getId().toString());
        courseContent.setDbId(course.getId());
        courseContent.setTitle(course.getTitle());
        courseContent.setDescription(course.getDescription());
        courseContent.setOutcome(course.getCourseOutcome());
        courseContent.setCreatorName(user.getFullName());
        courseContent.setTags(tags.stream().map(tag -> tag.getName()).collect(Collectors.toList()));
        courseContent.setCourseUrl(this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE).getUrl());
        courseContent.setThumbnailUrl(course.getThumbnail());
        courseContent.setUserProfileUrl(userProfile.getProfileUrl());
        courseContent.setUserPictureUrl(userProfile.getProfilePicture());
        courseContent.setCreatedBy(course.getCreatedBy());
        courseContent.setStatus(course.getCourseStatus().name());
        courseContent.setSections(this.createESSections(sections));

        this.esCourseContentService.save(Arrays.asList(courseContent));
    }

    List<SectionContent> createESSections(List<SectionDetail> sections){
        List<SectionContent> sectionContents = new ArrayList<>();
        sections.forEach(section -> {
            SectionContent sectionContent = SectionContent
                    .builder()
                    .id(section.getSectionId().toString())
                    .sectionId(section.getSectionId())
                    .name(section.getSectionName())
                    .status(section.getDelete())
                    .topics(this.createESTopics(section.getTopicDetails(), section.getSectionId()))
                    .build();
            sectionContents.add(sectionContent);
        });
        return sectionContents;
    }

    List<TopicContent> createESTopics(List<TopicDetail> topicDetails, Long sectionId){
        List<TopicContent> topicContents = new ArrayList<>();
        topicDetails.forEach(topic -> {
            String data = null;
            if(Objects.nonNull(topic.getVideoTranscribe())){
                data = topic.getVideoTranscribe();
            }else if(Objects.nonNull(topic.getArticle())){
                data = topic.getArticle();
            }
            TopicContent topicContent = TopicContent.
                    builder()
                    .id(topic.getTopicId().toString())
                    .topicId(topic.getTopicId())
                    .sectionId(sectionId)
                    .name(topic.getTopicName())
                    .data(data)
                    .status(topic.getDelete())
                    .build();
            topicContents.add(topicContent);
        });
        return topicContents;
    }

    public CourseContent getCourseContentById(String courseId) {
        return this.esCourseContentService.getCourseContentById(courseId);
    }

    public Message<Page<CourseContentResponse>> searchCourses(String query, int pageNo, int pageSize, String email) throws BadRequestException, EntityNotFoundException, InternalServerException {

        SearchPage<CourseContent> result = this.esCourseContentService.searchCourses(query, pageNo, pageSize);
        if (!result.getContent().isEmpty()) {
            List<SearchHit<CourseContent>> courseHits = result.getSearchHits().getSearchHits();
            List<CourseContentResponse> courses = new ArrayList<>();
            for (SearchHit<CourseContent> c : courseHits) {

                try{
                    CourseDetailResponse courseDetail = this.getCourseDetailsById(Long.valueOf(c.getContent().getId()), null).getData();

                    List<SectionContentResponse> sectionContentResponseList = !c.getInnerHits().get("highlightedSections").getSearchHits().isEmpty() ? this.getESSectionContentResponse(c.getInnerHits().get("highlightedSections").getSearchHits()) : new ArrayList<>();
                    List<TopicContentResponse> topicContentResponseList = !c.getInnerHits().get("highlightedTopics").getSearchHits().isEmpty() ? this.getESTopicContentResponse(c.getInnerHits().get("highlightedTopics").getSearchHits()) : new ArrayList<>();
                    CourseContentResponse contentResponse = CourseContentResponse
                            .builder()
                            .id(c.getContent().getId())
                            .title(c.getContent().getTitle())
                            .outcome(c.getContent().getOutcome())
                            .description(c.getContent().getDescription())
                            .tags(c.getContent().getTags())
                            .courseUrl(c.getContent().getCourseUrl())
                            .creatorName(courseDetail.getCreatorName())
                            .userProfileUrl(courseDetail.getUserProfileUrl())
                            .profilePictureUrl(courseDetail.getProfilePicture())
                            .thumbnailUrl(c.getContent().getThumbnailUrl())
                            .rating(courseDetail.getReview())
                            .numberOfViewers(courseDetail.getNoOfReviewers())
                            .duration(courseDetail.getCourseDuration())
                            .courseType(courseDetail.getCourseType().equals(CourseType.PREMIUM_COURSE.name()) ? "PREMIUM" :
                                    courseDetail.getCourseType().equals(CourseType.STANDARD_COURSE.name()) ? "STANDARD" :
                                            courseDetail.getCourseType().equals(CourseType.FREE_COURSE.name()) ? "FREE" : "")
                            .price(courseDetail.getPrice())
                            .isEnrolled(Objects.nonNull(email) && this.enrollmentService.isEnrolled(Long.parseLong(c.getContent().getId()), email))
                            .contentType(courseDetail.getContentType())
                            .testTotalQuestion(courseDetail.getTestTotalQuestion())
                            .build();

                    List<ScoredItem> scoredItems = new ArrayList<>();
                    sectionContentResponseList.forEach(s -> scoredItems.add(ScoredItem.builder().id(s.getId()).name(s.getName()).sectionId(s.getSectionId()).score(s.getScore()).type("SECTION").build()));
                    topicContentResponseList.forEach(t -> scoredItems.add(ScoredItem.builder().id(t.getId()).name(t.getName()).topicId(t.getTopicId()).sectionId(t.getSectionId()).score(t.getScore()).type("TOPIC").build()));
                    scoredItems.sort(Comparator.comparingDouble(ScoredItem::getScore).reversed());
                    List<ScoredItem> topItems = scoredItems.stream().limit(5).collect(Collectors.toList());
                    sectionContentResponseList = new ArrayList<>();
                    topicContentResponseList = new ArrayList<>();
                    for(ScoredItem topItem: topItems){
                        if(topItem.getType().equalsIgnoreCase("SECTION")){
                            sectionContentResponseList.add(SectionContentResponse
                                    .builder()
                                    .id(topItem.getId())
                                    .name(topItem.getName())
                                    .sectionId(topItem.getSectionId())
                                    .score(topItem.getScore())
                                    .build());
                        }else {
                            topicContentResponseList.add(TopicContentResponse
                                    .builder()
                                    .id(topItem.getId())
                                    .name(topItem.getName())
                                    .topicId(topItem.getTopicId())
                                    .sectionId(topItem.getSectionId())
                                    .score(topItem.getScore())
                                    .build());
                        }
                    }
                    contentResponse.setSections(sectionContentResponseList);
                    contentResponse.setTopics(topicContentResponseList);
                    courses.add(contentResponse);
                } catch (Exception e) {
                    log.error("course not found with id: "+Long.valueOf(c.getContent().getId()));
                }
           }
           Pageable pageable = PageRequest.of(result.getPageable().getPageNumber(), result.getPageable().getPageSize());

           PageImpl page = new PageImpl<>(courses, pageable, result.getTotalElements());

           Message<Page<CourseContentResponse>> message = new Message<>();
           message.setCode(HttpStatus.OK.toString());
           message.setMessage("Courses fetched successfully.");
           message.setStatus(HttpStatus.OK.value());
           message.setData(page);
            return message;
        }
        throw new BadRequestException("Content not found");
    }

    private List<SectionContentResponse> getESSectionContentResponse(List<? extends SearchHit<?>> sectionsContent){
        List<SectionContentResponse> sections = new ArrayList<>();
        if(!sectionsContent.isEmpty()){
            for(SearchHit<?> s: sectionsContent){
                sections.add(SectionContentResponse
                        .builder()
                        .id(((SectionContent) s.getContent()).getId())
                        .sectionId(((SectionContent) s.getContent()).getSectionId())
                        .name(((SectionContent) s.getContent()).getName())
                        .score((double) s.getScore())
                        .build());
            }
        }
        return sections;
    }

    private List<TopicContentResponse> getESTopicContentResponse(List<? extends SearchHit<?>> topicsContent){
        List<TopicContentResponse> topics = new ArrayList<>();
        if(!topicsContent.isEmpty()){
            for(SearchHit<?> t: topicsContent){
                topics.add(TopicContentResponse
                        .builder()
                        .id(((TopicContent) t.getContent()).getId())
                        .topicId(((TopicContent) t.getContent()).getTopicId())
                        .sectionId(((TopicContent) t.getContent()).getSectionId())
                        .name(((TopicContent) t.getContent()).getName())
                        .score((double) t.getScore())
                        .build());
            }
        }
        return topics;
    }


    private void validateCreateCourseData(CreateCourseRequest request) throws BadRequestException {
        if (Stream.of(
                request.getTitle(),
                request.getDescription(),
                request.getCategoryId(),
                request.getCourseLevelId(),
                request.getAbout(),
                request.getThumbnailUrl(),
                request.getPrerequisite(),
                request.getCourseOutcomes(),
                request.getSections(),
                request.getTags()
        ).anyMatch(this::isNullOrEmpty) || request.getSections().isEmpty() || request.getTags().isEmpty()) {
            throw new BadRequestException("Please provide all required fields for course creation");
        }
    }

    private boolean isNullOrEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        return false;
    }

    public void validateAnswers(QuestionType questionType, List<CreateQuizQuestionAnswerRequest> answers) throws BadRequestException {
        if (answers == null || answers.isEmpty()) {
            log.warn("Validation failed: Answers list is empty.");
            throw new BadRequestException("Answers cannot be empty.");
        }

        long correctCount = answers.stream().filter(CreateQuizQuestionAnswerRequest::getIsCorrectAnswer).count();
        log.info("Validating answers for question type: {}. Correct answers count: {}", questionType, correctCount);

        switch (questionType) {
            case MULTIPLE_CHOICE:
                if (correctCount == 0) {
                    log.warn("Validation failed: Multiple choice question must have at least one correct answer.");
                    throw new BadRequestException("Multiple choice questions must have at least one correct answer.");
                }
                break;

            case SINGLE_CHOICE:
                if (correctCount != 1) {
                    log.warn("Validation failed: Single choice question must have exactly one correct answer. Found: {}", correctCount);
                    throw new BadRequestException("Single choice questions must have exactly one correct answer.");
                }
                break;

            case TRUE_FALSE:
                if (answers.size() != 2) {
                    log.warn("Validation failed: True/False question must have exactly two answers. Found: {}", answers.size());
                    throw new BadRequestException("True/False questions must have exactly two answers.");
                }
                List<String> answerTexts = answers.stream().map(CreateQuizQuestionAnswerRequest::getAnswerText).toList();
                log.info("True/False question answers: {}", answerTexts);

                if (!answerTexts.contains("True") || !answerTexts.contains("False")) {
                    log.warn("Validation failed: True/False answers must be 'True' and 'False'. Found: {}", answerTexts);
                    throw new BadRequestException("True/False answers must be 'True' and 'False'.");
                }
                if (correctCount != 1) {
                    log.warn("Validation failed: True/False question must have exactly one correct answer. Found: {}", correctCount);
                    throw new BadRequestException("True/False questions must have exactly one correct answer.");
                }
                break;

            case TEXT_FIELD:
                if (answers.size() != 1) {
                    log.warn("Validation failed: Text field question must have only one answer. Found: {}", answers.size());
                    throw new BadRequestException("Text field questions must have only one answer.");
                }
                log.info("Text field validation passed.");
                break;

            default:
                log.warn("Validation failed: Invalid question type {}", questionType);
                throw new BadRequestException("Invalid question type.");
        }

        log.info("Validation passed for question type: {}", questionType);
    }

    private List<SectionDetail> savingSectionAndTopics(CreateCourseRequest request, Course course)
            throws InternalServerException, EntityNotFoundException, BadRequestException {
        List<SectionDetail> savedSections = new ArrayList<>();
        int courseDurationCounter = 0;
        if (CollectionUtils.isEmpty(request.getSections()))
            throw new BadRequestException("No section data is provided.");
        for (CreateSectionRequest e : request.getSections()) {
            int sectionDurationCounter = 0;
            // Create Section
            Section populatedSection = Section.builder()
                    .id(e.getId())
                    .delete(e.getDelete())
                    .name(e.getTitle())
                    .course(course)
                    .sequenceNumber(e.getLevel())
                    .isFree(CourseType.valueOf(request.getCourseType()) != CourseType.PREMIUM_COURSE && e.getIsFree() != null && e.getIsFree())
                    .isActive(request.getIsActive())
                    .build();
            Section section = sectionService.save(populatedSection);

            if (Objects.isNull(section)) {
                continue;
            }

            SectionDetail sectionDetail = SectionDetail.builder().sectionId(section.getId()).sectionName(section.getName()).delete(section.isActive()).build();

            // Create Topic
            List<TopicDetail> savedTopics = new ArrayList<>();
            if(Objects.nonNull(e.getTopics()) && !e.getTopics().isEmpty()){
                for (CreateTopicRequest topic : e.getTopics()) {
                    TopicType topicType = topicTypeService.findById(topic.getTopicTypeId());
                    topic.setTopicTypeName(topicType.getName());
                    Topic savedTopic = topicService.save(Topic.builder()
                            .id(topic.getId())
                            .delete(topic.getDelete())
                            .sequenceNumber(topic.getLevel())
                            .name(topic.getTitle())
                            .section(section)
                            .topicType(topicType)
                            .durationInSec(
                                    topicType.getName().equals("Video")
                                            ? topic.getDuration()
                                            : topicType.getName().equals("Quiz")
                                            ? (topic.getQuiz().getDurationInMinutes() * 60)
                                            : 180
                            )

                            .creationDate(new Date())
                            .lastModifiedDate(Objects.isNull(topic.getId()) ? null : new Date())
                            .build());
                    if (Objects.isNull(savedTopic)) {
                        sectionDurationCounter += 0;
                        continue;
                    }
                    sectionDurationCounter += topic.getDuration();

                    TopicDetail topicDetail = TopicDetail
                            .builder()
                            .topicId(savedTopic.getId())
                            .topicName(savedTopic.getName())
                            .delete(savedTopic.getDelete())
                            .build();

                    if (Objects.nonNull(topic.getQuiz())) {
                        if (!Objects.isNull(topic.getQuiz().getQuestions()) && !topic.getQuiz().getQuestions().isEmpty()) {
                            Quiz quiz = quizService.save(Quiz.builder()
                                    .id(topic.getQuiz().getId())
                                    .delete(topic.getQuiz().getDelete())
                                    .topic(savedTopic)
                                    .title(topic.getTitle())
                                    .durationInMinutes(topic.getQuiz().getDurationInMinutes()==null?0:topic.getQuiz().getDurationInMinutes())
                                    .passingCriteria(topic.getQuiz().getPassingCriteria()==null?0:topic.getQuiz().getPassingCriteria())
                                    .randomQuestion(topic.getQuiz().getRandomQuestion())
                                    .build());
                            for (CreateQuizQuestionRequest question : topic.getQuiz().getQuestions()) {
                                if (question.getQuestionType()==null){
                                    throw new BadRequestException("Quiz question type must have be required");
                                }
                                QuizQuestion quizQuestion = quizQuestionService.save(QuizQuestion.builder()
                                        .id(question.getId())
                                        .delete(question.getDelete())
                                        .quiz(quiz)
                                        .questionText(question.getQuestionText())
                                        .explanation(question.getExplanation())
                                        .questionType(question.getQuestionType())
                                        .build());

                                if (Objects.isNull(quizQuestion))
                                    continue;

                                if (!question.getDelete()) {
                                    if (Objects.isNull(question.getAnswers()) || question.getAnswers().isEmpty()) {
                                        throw new BadRequestException("Quiz question answers cannot be empty");
                                    }

                                    if (!isCorrectAnswerExist(question)) {
                                        throw new BadRequestException("Quiz question answers must have at least one correct answer");
                                    }
                                }
                                validateAnswers(quizQuestion.getQuestionType(), question.getAnswers());
                                for (CreateQuizQuestionAnswerRequest answer : question.getAnswers()) {
                                    QuizQuestionAnwser quizQuestionAnwser = quizQuestionAnswerService.save(
                                            QuizQuestionAnwser.builder()
                                                    .id(answer.getId())
                                                    .delete(answer.getDelete())
                                                    .quizQuestion(quizQuestion)
                                                    .answer(answer.getAnswerText())
                                                    .isCorrectAnswer(answer.getIsCorrectAnswer())
                                                    .build()
                                    );
                                }
                            }
                        } else {
                            throw new BadRequestException("Quiz questions can not be empty");
                        }
                    }
                    if (Objects.nonNull(topic.getVideo())) {
                        if (!Objects.isNull(topic.getVideo().getVideoURL()) && !topic.getVideo().getVideoURL().isEmpty()) {
                            Video video = videoService.save(Video.builder()
                                    .id(topic.getVideo().getId())
                                    .delete(topic.getVideo().getDelete())
                                    .filename(topic.getVideo().getFilename())
                                    .topic(savedTopic)
                                    .videoURL(topic.getVideo().getVideoURL())
                                    .uploadedDate(new Date())
                                    .summary(topic.getVideo().getSummary())
                                    .transcribe(topic.getVideo().getTranscribe())
                                    .vttContent(topic.getVideo().getVttContent())
                                    .build());

                            topicDetail.setVideoTranscribe(video.getTranscribe());

                            if (!CollectionUtils.isEmpty(topic.getVideo().getDocuments())) {
                                List<Document> documents = topic.getVideo().getDocuments()
                                        .stream()
                                        .map(videoDoc -> Document.builder()
                                                .id(videoDoc.getId())
                                                .delete(videoDoc.getDelete())
                                                .name(videoDoc.getDocName())
                                                .url(videoDoc.getDocUrl())
                                                .video(video)
                                                .summary(videoDoc.getSummary())
                                                .build())
                                        .toList();
                                documentService.saveAll(documents);
                            }
                            savedTopic.setVideos(Arrays.asList(video));
                        } else {
                            throw new BadRequestException("Video url cannot be empty");
                        }
                    }
                    if (Objects.nonNull(topic.getArticle())) {
                        if (!Objects.isNull(topic.getArticle().getArticle()) && topic.getArticle().getArticle().length() != 0) {
                            Article article = articleService.save(Article.builder()
                                    .id(topic.getArticle().getId())
                                    .delete(topic.getArticle().getDelete())
                                    .content(HtmlUtils.htmlEscape(topic.getArticle().getArticle()))
                                    .topic(savedTopic)
                                    .build());
                            topicDetail.setArticle(article.getContent());

                            if (!CollectionUtils.isEmpty(topic.getArticle().getDocuments())) {
                                List<Document> documents = topic.getArticle().getDocuments()
                                        .stream()
                                        .map(articleDoc -> Document.builder()
                                                .id(articleDoc.getId())
                                                .delete(articleDoc.getDelete())
                                                .article(article)
                                                .name(articleDoc.getDocName())
                                                .url(articleDoc.getDocUrl())
                                                .summary(articleDoc.getSummary())
                                                .build())
                                        .toList();
                                documentService.saveAll(documents);
                            }
                            savedTopic.setArticles(Arrays.asList(article));
                        } else {
                            throw new BadRequestException("Article content cannot be empty");
                        }
                    }

                    savedTopics.add(topicDetail);
                }
            }
            courseDurationCounter += sectionDurationCounter;
            sectionDetail.setTopicDetails(savedTopics);
            savedSections.add(sectionDetail);
        }

        int durationInHours = Math.max((courseDurationCounter / 60) / 60, 1);
        course.setCourseDurationInHours(durationInHours);

        repo.save(course);
        return savedSections;
    }

    private Boolean isCorrectAnswerExist(CreateQuizQuestionRequest quizQuestionRequest) {
        return quizQuestionRequest.getAnswers().stream().anyMatch(a -> a.getIsCorrectAnswer());
    }

    @Transactional
    @Modifying
    private Course savingCourse(CreateCourseRequest request, User instructor, CourseLevel courseLevel, CourseCategory courseCategory) throws InternalServerException {
        try {
            StringBuilder documentVector = new StringBuilder();
            documentVector.append(request.getTitle()).append(' ');
            documentVector.append(request.getAbout()).append(' ');
            documentVector.append(Objects.isNull(courseCategory) ? ' ' : courseCategory.getName()).append(' ');
            request.getTags().forEach(e -> documentVector.append(e.getName()).append(' '));

            Course course = new Course();
            if (Objects.nonNull(request.getCourseId())) {
                course = repo.findById(request.getCourseId()).orElseThrow(() -> new EntityNotFoundException("No course found by provided course id."));
            }

            // Check whether each section is free for the course if yes then add courseType as free
            boolean isFree = Objects.nonNull(request.getSections()) && !request.getSections().isEmpty() && request.getSections().stream().allMatch(CreateSectionRequest::getIsFree);
            CourseType courseType = null;

            if (Objects.nonNull(request.getCourseType())) {
                if (CourseType.valueOf(request.getCourseType()) == CourseType.STANDARD_COURSE && isFree)
                    courseType = CourseType.FREE_COURSE;
                else if (CourseType.valueOf(request.getCourseType()) == CourseType.FREE_COURSE && !isFree)
                    courseType = CourseType.STANDARD_COURSE;
                else {
                    courseType = CourseType.valueOf(request.getCourseType());
                }
            }

            course = Course.builder()
                    .id(Objects.isNull(course) ? null : course.getId())
                    .courseType(courseType)
                    .price(request.getPrice() != null
                            ? (request.getPrice() > Math.floor(request.getPrice() * 100) / 100
                            ? Math.floor(request.getPrice() * 100) / 100
                            : request.getPrice())
                            : null)
                    .about(request.getAbout())
                    .courseLevel(courseLevel)
                    .courseCategory(courseCategory)
                    .courseDurationInHours(request.getCourseDuration())
                    .instructor(instructor)
                    .description(request.getDescription())
                    .title(Objects.nonNull(request.getTitle()) ? request.getTitle().trim() : Objects.nonNull(course) ? course.getTitle() : null)
                    .prerequisite(!CollectionUtils.isEmpty(request.getPrerequisite()) ? String.join("~", request.getPrerequisite()).trim() : null)
                    .courseOutcome(!CollectionUtils.isEmpty(request.getCourseOutcomes()) ? String.join("~", request.getCourseOutcomes()).trim() : null)
                    .previewVideoURL(request.getPreviewVideoURL())
                    .previewVideoVttContent(request.getPreviewVideoVttContent())
                    .thumbnail(request.getThumbnailUrl())
                    .documentVector(documentVector.toString())
                    .certificateEnabled(!Objects.isNull(request.getCertificateEnabled()) && request.getCertificateEnabled())
                    .courseStatus(request.getIsActive() ? CourseStatus.PUBLISHED : CourseStatus.DRAFT)
                    .courseProgress(request.getIsActive() ? "100" : this.calculateCompleteCourseProgress(request))
                    .contentType(Objects.nonNull(request.getContentType()) ? ContentType.valueOf(request.getContentType()) : null)
                    .metaDescription(Objects.nonNull(course.getMetaDescription()) ? course.getMetaDescription() : null)
                    .metaTitle(Objects.nonNull(course.getMetaTitle()) ? course.getMetaTitle() : "")
                    .metaHeading(Objects.nonNull(course.getMetaHeading()) ? course.getMetaHeading() : "")
//                    .courseUrl(request.getIsActive() && Objects.isNull(course.getCourseUrl()) ? generateCourseUrl(request.getTitle()): course.getCourseUrl())
                    .build();

            course.setCreationDate(new Date());
            course.setCreatedBy(instructor.getId());
            course.setModifiedBy(instructor.getId());
            course.setLastModifiedDate(new Date());
            if (course.getCourseStatus().equals(CourseStatus.DRAFT)) {
                this.courseUrlService.deleteCourseUrlByCourseId(course.getId());
            }
            Course saved = repo.save(course);
            CourseUrl courseUrl = new CourseUrl();
            if (Objects.nonNull(request.getCourseUrl())) {
                courseUrl = CourseUrl.builder()
                        .status(GenericStatus.ACTIVE)
                        .url(request.getCourseUrl().trim().toLowerCase())
                        .course(course)
                        .build();
                this.courseUrlService.save(courseUrl);
            }

//            com.vinncorp.fast_learner.es_models.Course esCourse = null;
//            if (request.getIsActive() && Objects.nonNull(request.getCourseId())) {
//                try {
//                    esCourse = esCourseService.findByDBId(course.getId());
//                } catch (EntityNotFoundException ex) {
//                    esCourse = new com.vinncorp.fast_learner.es_models.Course();
//                }
//            } else {
//                esCourse = new com.vinncorp.fast_learner.es_models.Course();
//            }
//            if (request.getIsActive()) {
//                esCourse.setId(Objects.isNull(esCourse.getId()) ? null : esCourse.getId());
//                esCourse.setDbId(saved.getId());
//                esCourse.setTitle(saved.getTitle().trim());
//                esCourse.setCourseUrl(courseUrl.getUrl());
//                esCourse.setDocVector(saved.getDocumentVector());
//                esCourse.setThumbnail(saved.getThumbnail());
//                esCourse.setCourseStatus(CourseStatus.PUBLISHED.toString());
//                esCourseService.save(esCourse);
//            }

            return saved;
        } catch (Exception e) {
            log.error("ERROR: " + e.getLocalizedMessage());
            throw new InternalServerException("Course cannot be saved due to database error.");
        }
    }

    @Transactional
    private String generateCourseUrl(String courseTitle) throws EntityNotFoundException {
        System.out.println(courseTitle);
        // Define specific replacements
        Map<String, String> replacements = new HashMap<>();
        replacements.put("\\+", "plus");
        replacements.put("&", "and");
        replacements.put("@", "at");
        replacements.put("#", "hash");
        replacements.put("=", "equals");
        replacements.put("%", "percent");

        // Apply each replacement
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            courseTitle = courseTitle.replaceAll(entry.getKey(), entry.getValue());
        }
        String noSpecialChars = courseTitle.replaceAll("[^a-zA-Z0-9\\s]", "");
        String courseUrl = noSpecialChars.trim().replaceAll("\\s+", "-").toLowerCase();
        Boolean urlUnique = false;
        int counter = 0;
        while (!urlUnique) {
            String finalUrl = courseUrl + (counter == 0 ? "" : "-" + counter);
            if (Objects.isNull(this.courseUrlService.findByUrlAndCourseStatuses(finalUrl, Arrays.asList(CourseStatus.PUBLISHED, CourseStatus.UNPUBLISHED)))) {
                urlUnique = true;
                courseUrl = finalUrl;
            } else {
                counter++;
            }
        }
        return courseUrl;
    }

    private String calculateCompleteCourseProgress(CreateCourseRequest course) {
        Double courseProgress = this.calculateCourseProgress(course);
        Double sectionProgress = this.calculateSectionProgress(course.getSections());
        Double completeCourseProgress = courseProgress + sectionProgress;
        return completeCourseProgress.toString();
    }

    private Double calculateCourseProgress(CreateCourseRequest course) {
        Double totalProgress = 0.0;
        Double progressPerField = 3.3;
        List<CreateTagRequest> tagRequests = course.getTags().stream().filter(t -> t.getActive()).collect(Collectors.toList());
        if (!Objects.isNull(course.getTitle())) {
            totalProgress += progressPerField;
        }
        if (!Objects.isNull(course.getDescription())) {
            totalProgress += progressPerField;
        }
        if (!Objects.isNull(course.getCategoryId())) {
            totalProgress += progressPerField;
        }
        if (!Objects.isNull(course.getCourseLevelId())) {
            totalProgress += progressPerField;
        }
        if (!Objects.isNull(course.getAbout())) {
            totalProgress += progressPerField;
        }
        if (!Objects.isNull(course.getThumbnailUrl())) {
            totalProgress += progressPerField;
        }
        if (!Objects.isNull(course.getPreviewVideoURL())) {
            totalProgress += progressPerField;
        }
        if (tagRequests.size() > 0) {
            totalProgress += progressPerField;
        }
        if (course.getPrerequisite().size() > 0) {
            totalProgress += progressPerField;
        }
        if (course.getCourseOutcomes().size() > 0) {
            totalProgress += progressPerField;
        }

        BigDecimal bd = BigDecimal.valueOf(totalProgress);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public Double calculateSectionProgress(List<CreateSectionRequest> sections) {
        Double totalProgress = 0.0;

        if (sections != null && !sections.isEmpty()) {
            Optional<CreateSectionRequest> optionalSection = sections.stream()
                    .filter(s -> !s.getDelete() && s.getTopics() != null && s.getTopics().stream().anyMatch(t -> !t.getDelete()))
                    .findFirst();

            if (optionalSection.isPresent()) {
                CreateSectionRequest validSection = optionalSection.get();

                if (validSection.getTitle() != null && !validSection.getTitle().isEmpty()) {
                    totalProgress += 4;
                }
                if (validSection.getTopics() != null && !validSection.getTopics().isEmpty()) {
                    totalProgress += 30;
                }
            }
        }

        BigDecimal bd = BigDecimal.valueOf(totalProgress);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public Message<CourseByCategoryPaginatedResponse> getCoursesByCategoryWithPagination(CourseByCategoryRequest request, String email) throws EntityNotFoundException {
        log.info("Fetching courses by category with pagination.");

        // Whenever we have to do nothing on catch block if any exception is thrown then we have to use
        // ExceptionUtils.safeFetch() utility method so we can avoid multiple try catch blocks for each method.
        User user = ExceptionUtils.safelyFetch(() -> userService.findByEmail(email));
        CourseCategory category = ExceptionUtils.safelyFetch(() -> courseCategoryService.findById(request.getCategoryId()));
        CourseLevel courseLevel = ExceptionUtils.safelyFetch(() -> courseLevelService.findById(request.getCourseLevelId()));

        // Fetching all courses by most reviewed courses
        // Fetching by the category if provided if not then fetch from all courses
        Page<Tuple> data = repo.findAllByCoursesCategoryOrCourseIdAndMostReviewed(
                category == null ? null : category.getId(), courseLevel == null ? null : courseLevel.getId(), null, Objects.isNull(user) ? null : user.getId(),
                PageRequest.of(request.getPageNo(), request.getPageSize()));

        if (data.isEmpty()) {
            throw new EntityNotFoundException("No courses found.");
        }
        List<CourseByCategoryResponse> mappedData = getCourseByCategoryResponses(data);


        CourseByCategoryPaginatedResponse response = CourseByCategoryPaginatedResponse.builder()
                .data(mappedData)
                .pageNo(request.getPageNo())
                .pageSize(request.getPageSize())
                .pages(data.getTotalPages())
                .totalElements(data.getTotalElements())
                .build();
        // Manually set the parent fields (previousPage, currentPage, nextPage)
        response.setPreviousPage(request.getPageNo() > 0 ? (long) request.getPageNo() - 1 : null);
        response.setCurrentPage((long) request.getPageNo());
        response.setNextPage(request.getPageNo() < data.getTotalPages() - 1 ? (long) request.getPageNo() + 1 : null);

        return new Message<CourseByCategoryPaginatedResponse>()
                .setData(response)
                .setMessage("Courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());

    }

    @Override
    public Message<CourseByCategoryPaginatedResponse> getCoursesByInstructorForProfile(
            Long instructorId, int pageNo, int pageSize, String email) throws EntityNotFoundException {
        log.info("Fetching courses by teacher for profile with pagination.");
        User user = null;
        if (Objects.nonNull(email)) {
            user = userService.findByEmail(email);
        }
        Long userId = (user != null) ? user.getId() : null;

        // Fetching all courses by most reviewed courses
        // Fetching by the category if provided if not then fetch from all courses
        Page<Tuple> data = repo.findAllByInstructorAndLoggedInUser(
                Objects.isNull(instructorId) ? user.getId() : instructorId, userId, PageRequest.of(pageNo, pageSize));

        if (data.isEmpty()) {
            throw new EntityNotFoundException("No courses found.");
        }
        List<CourseByCategoryResponse> mappedData = getCourseByCategoryResponses(data);

        return new Message<CourseByCategoryPaginatedResponse>()
                .setData(CourseByCategoryPaginatedResponse.builder()
                        .data(mappedData)
                        .pageNo(pageNo)
                        .pageSize(pageSize)
                        .pages(data.getTotalPages())
                        .totalElements(data.getTotalElements())
                        .build()
                )
                .setMessage("Courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());


    }

    @Override
    public Course findById(Long courseId) throws EntityNotFoundException {
        log.info("Fetching course by id: " + courseId);
        return repo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found by course id: " + courseId));
    }

    @Override
    public Message<CourseDetailResponse> getCourseDetailsById(Long courseId, String email) throws EntityNotFoundException, InternalServerException, BadRequestException {
        log.info("Fetching course details by course id: " + courseId);
        User user = null;
        try {
            if (Objects.nonNull(email))
                user = userService.findByEmail(email);
        } catch (Exception e) {
        }
        Page<Tuple> courseDetails = repo.findAllByCoursesCategoryOrCourseIdAndMostReviewed(
                null, null, courseId, Objects.nonNull(user) ? user.getId() : null,
                PageRequest.of(0, 1));
        if (courseDetails.isEmpty()) {
            throw new EntityNotFoundException("No courses found.");
        }
        CourseDetailResponse courseDetailResponse = CourseDetailResponse.from(courseDetails.getContent().get(0));

        log.info("Fetching enrolled users in the instructor's course and total no of courses of the instructor.");
        Tuple userStats = repo.findCoursesAndStudentEnrolledByUserId(courseDetailResponse.getUserId());
        courseDetailResponse.setTotalCourses(userStats.get("courses") != null ? Integer.parseInt("" + userStats.get("courses")) : 0);
        courseDetailResponse.setTotalEnrolled(userStats.get("enrolled_students") != null ? Integer.parseInt("" + userStats.get("enrolled_students")) : 0);

        log.info("Fetching section and topic details of the course.");
        List<SectionDetail> sectionDetails = sectionService.fetchSectionDetailByCourseId(courseId);
        int totalTopics = sectionDetails.stream().mapToInt(e -> e.getTopicDetails().size()).sum();
        courseDetailResponse.setNoOfTopics(totalTopics);
        courseDetailResponse.setSectionDetails(sectionDetails);
        courseDetailResponse.setCourseDuration(sectionDetails.stream().mapToInt(SectionDetail::getSectionDuration).sum());

        // Fetching feedback data
        log.info("Fetching feedback of users for the course and review's status.");
        if (courseDetailResponse.getNoOfReviewers() > 0)
            populateStudentFeedbacks(courseId, courseDetailResponse);

        log.info("Fetching tags for the course.");
        List<Tag> tags = tagService.findByCourseId(courseId);
        if (!CollectionUtils.isEmpty(tags)) {
            courseDetailResponse.setTags(tags.stream().map(Tag::getName).toList());
        }

        // save course visitor
        if (Objects.nonNull(user))
            courseVisitorService.save(repo.findById(courseId).get(), user);

        return new Message<CourseDetailResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Course details fetched successfully.")
                .setData(courseDetailResponse);
    }

    private void populateStudentFeedbacks(Long courseId, CourseDetailResponse courseDetailResponse) throws EntityNotFoundException {
        Message<CourseFeedbackResponse> feedback = courseReviewService.findStudentFeedbackByCourseId(courseId, 0, 15);
        courseDetailResponse.setCourseFeedback(feedback.getData().getFeedback());
    }

    @Override
    public Message<RelatedCoursesResponse> getRelatedCoursesByPagination(RelatedCoursesRequest request, String email) throws EntityNotFoundException {
        log.info("Fetching related courses.");
        User user = null;
        try {
            if (Objects.nonNull(email)) {
                user = userService.findByEmail(email);
            }
        } catch (Exception e) {
        }
        Long userId = (user != null) ? user.getId() : null;

        Course course = this.findById(request.getCourseId());
        List<Tag> tags = tagService.findByCourseId(course.getId());
        String tagStringify = tags.stream().map(Tag::getName).collect(Collectors.joining(" "));

        Page<Tuple> pagedData = repo.findRelatedCourses(
                request.getCourseId(),
                course.getTitle() + " " + course.getCourseCategory().getName() + " " + tagStringify, userId,
                PageRequest.of(request.getPageNo(), request.getPageSize()));

        if (Objects.isNull(pagedData) || pagedData.isEmpty()) {
            throw new EntityNotFoundException("No related courses or alternate sections available.");
        }

        RelatedCoursesResponse response = RelatedCoursesResponse.builder()
                .courses(RelatedCourses.from(pagedData.getContent()))
                .pageNo(request.getPageNo())
                .pageSize(request.getPageSize())
                .totalPages(pagedData.getTotalPages())
                .totalElements(pagedData.getTotalElements())
                .build();

        return new Message<RelatedCoursesResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Related courses and alternate sections fetched successfully.")
                .setData(response);
    }


    @Override
    public Message<CourseBySearchFilterResponse> searchCourse(SearchCourseRequest request, String email) throws EntityNotFoundException {
        log.info("Searching courses by filters.");
        User user = null;
        try {
            if (email != null)
                user = userService.findByEmail(email);
        } catch (Exception e) {
        }
        String searchInputForDB;
        if (Objects.nonNull(request.getSearchValue()) && !request.getSearchValue().equals("")) {
            searchInputForDB = "%" + request.getSearchValue() + "%";
        } else {
            searchInputForDB = null;
            request.setSearchValue(null);
        }

        List<Long> courseIds = new ArrayList<>();

        List<CourseResponse> nlpSearchData = null;
        if (Objects.nonNull(request.getIsNlpSearch()) && request.getIsNlpSearch()) {
            nlpSearchData = fetchNLPSearch(request.getSearchValue());
            courseIds = nlpSearchData.stream().map(res -> Long.valueOf(res.getCourseId())).toList();
        }

        Page<Tuple> data = repo.findAllBySearchFilter(
                searchInputForDB, request.getReviewFrom(), request.getReviewTo(), Objects.isNull(user) ? null : user.getId(), courseIds.isEmpty(), courseIds,
                PageRequest.of(request.getPageNo(), request.getPageSize()));

        List<CourseByCategoryResponse> mappedData = getCourseByCategoryResponses(data);

        if (data.isEmpty() && Objects.isNull(nlpSearchData) && mappedData.isEmpty()) {
            throw new EntityNotFoundException("No data found");
        }
        return new Message<CourseBySearchFilterResponse>()
                .setData(CourseBySearchFilterResponse.builder()
                        .courses(mappedData)
                        .nlpCourses(nlpSearchData)
                        .pageNo(request.getPageNo())
                        .pageSize(request.getPageSize())
                        .pages(data.getTotalPages())
                        .totalElements(data.getTotalElements())
                        .build()
                )
                .setMessage("Courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }

    private List<CourseResponse> fetchNLPSearch(String searchValue) {
        log.info("Searching by nlp...");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", AUTH_TOKEN);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", searchValue);

        HttpEntity<Map> entity = new HttpEntity<>(requestBody, httpHeaders);
        ResponseEntity<List<CourseResponse>> response = null;
        try {
            response = restTemplate.exchange(NLP_SEARCH_PYTHON_SERVICE, HttpMethod.POST, entity, new ParameterizedTypeReference<List<CourseResponse>>() {
            });
        } catch (Exception e) {
            log.error("ERROR FETCHING NLP: " + e.getLocalizedMessage());
        }
        if (Objects.nonNull(response) && response.getStatusCode().is2xxSuccessful())
            return response.getBody();

        return null;
    }

    @NotNull
    public List<CourseByCategoryResponse> getCourseByCategoryResponses(Page<Tuple> data) throws EntityNotFoundException {
        if (Objects.isNull(data) || data.getContent().isEmpty())
            return new ArrayList<>();
        List<CourseByCategoryResponse> mappedData = CourseByCategoryResponse.from(data.getContent());

        List<NoOfTopicInCourse> allTopicByCourses = topicService.getAllTopicByCourses(
                mappedData.stream().map(CourseByCategoryResponse::getCourseId).toList());

        mappedData = mappedData.stream()
                .peek(e -> allTopicByCourses.stream()
                        .filter(a -> e.getCourseId().equals(a.getCourseId()))
                        .findAny()
                        .ifPresent(matchedCourse -> {
                            e.setNoOfTopics(matchedCourse.getTopics());
                            e.setCourseDuration(matchedCourse.getDuration());
                        }))
                .collect(Collectors.toList());

        return mappedData;
    }

    @Override
    public Message<TeacherCoursesResponse> findCoursesByTeacher(String searchInput, Integer sort, int pageNo, int pageSize, String email)
            throws EntityNotFoundException, BadRequestException {
        log.info("Fetching courses of teacher: " + email);

        if (pageNo < 0 || pageSize < 0) {
            throw new BadRequestException("Page no or Page size cannot be negative");
        }

        if (sort == null || (sort != 0 && sort != 1)) {
            throw new BadRequestException("Sort can only be 0 or 1");
        }

        if (searchInput == null) {
            searchInput = "";
        }

        User user = userService.findByEmail(email);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        if (sort == null)
            sort = 0;

        Page<Tuple> courses = repo.findAllCoursesWithFilter(user.getId(), sort, searchInput, pageable);

        if (courses.isEmpty()) {
            throw new EntityNotFoundException("No result found for provided filters.");
        }

        TeacherCoursesResponse response = TeacherCoursesResponse.from(courses);

        return new Message<TeacherCoursesResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Fetched teachers courses successfully.")
                .setData(response);
    }

    @Override
    public void sendCourseSharedNotification(Long courseId, String email) throws EntityNotFoundException {
        log.info("Sending notification of course sharing by user: " + email);
        User user = userService.findByEmail(email);
        Course course = repo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found by provided id."));
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
        rabbitMQProducer.sendMessage(course.getTitle(), "student/course-details/" + courseUrl.getUrl(), user.getEmail(),
                course.getCreatedBy(), course.getContentType(), NotificationContentType.TEXT, NotificationType.COURSE_SHARE, course.getId());
    }

    @Override
    public Message<CourseDetailForUpdateResponse> fetchCourseDetailForUpdateForFirstStep(Long courseId, String email)
            throws EntityNotFoundException, BadRequestException {
        log.info("Fetching course detail for update for first step.");
        if (Objects.isNull(courseId)) {
            throw new BadRequestException("Course ID cannot be null");
        }
        if (Objects.isNull(email)) {
            throw new BadRequestException("Email cannot be null");
        }

        User user = userService.findByEmail(email);
        Course course = repo.findByIdAndCreatedBy(courseId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("No course found for the logged in user."));
        List<Tag> tags = tagService.findByCourseId(courseId);

        CourseDetailForUpdateResponse detail = CourseDetailForUpdateResponse.from(course, tags);
        CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
        detail.setCourseUrl(Objects.isNull(courseUrl) ? null : courseUrl.getUrl());

        return new Message<CourseDetailForUpdateResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Course detail for first step is fetched successfully.")
                .setData(detail);

    }

    @Override
    public boolean isCourseOwnedByUser(String email) {
        log.info("Course ownership is validating...");
        return repo.existsByUserEmail(email);
    }

    @Override
    public CourseDetailForCertificate getCourseDetailForCertificate(Long courseId) throws EntityNotFoundException {
        log.info("Fetching course details for a certificate of a user.");
        Tuple data = repo.fetchCourseDetailForCertificate(courseId);
        if (Objects.isNull(data)) {
            throw new EntityNotFoundException("Course detail is not found.");
        }
        return CourseDetailForCertificate.from(data);
    }

    @Override
    public boolean isExistCertificateEnabledByCourseId(Long courseId) {
        log.info("Validating certificate enabled for the provided course.");
        return repo.existsByIdAndCertificateEnabled(courseId, true);
    }

    @Override
    public Course findByDocVector(String docVector) {
        return this.repo.findByDocumentVector(docVector);
    }

    public Message<String> checkUniqueCourseTile(String title, Long courseId, Principal principal) throws BadRequestException, EntityNotFoundException {
        Boolean titleUnique = false;
        String courseTitle = title.trim();
        User user = this.userService.findByEmail(principal.getName());

        List<Course> courses = this.repo.findByTitle(courseTitle);
        Course course = courses.stream().filter(c -> c.getCourseStatus().equals(CourseStatus.PUBLISHED) || c.getCourseStatus().equals(CourseStatus.UNPUBLISHED)).findFirst().orElse(null);
        titleUnique = Objects.isNull(course) || course.getId().equals(courseId) ? true : false;

        if (titleUnique) {
            String generatedUrl = generateCourseUrl(title);
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Title is unique.")
                    .setData(generatedUrl);
        } else {
            throw new BadRequestException("Title already exists.");
        }
    }

    public Message<String> checkUniqueCourseUrl(String url, Long courseId, Principal principal) throws BadRequestException, EntityNotFoundException {
        Boolean urlAvailable = false;
        if (Objects.isNull(url)) {
            throw new BadRequestException("url cannot be empty");
        }
        User user = this.userService.findByEmail(principal.getName());
        CourseUrl courseUrl = this.courseUrlService.findByUrlAndCourseStatuses(url, Arrays.asList(CourseStatus.PUBLISHED, CourseStatus.UNPUBLISHED));
        if (Objects.isNull(courseUrl) ||
                (!courseUrl.getCourse().getCourseStatus().equals(CourseStatus.PUBLISHED) &&
                        !courseUrl.getCourse().getCourseStatus().equals(CourseStatus.UNPUBLISHED))) {
            urlAvailable = true;
        } else if ((courseUrl.getCourse().getCourseStatus().equals(CourseStatus.PUBLISHED) || courseUrl.getCourse().getCourseStatus().equals(CourseStatus.UNPUBLISHED)) &&
                courseUrl.getCourse().getId().equals(courseId)) {
            urlAvailable = true;
        }

        if (urlAvailable) {
            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Url is unique.")
                    .setData("Url is unique.");
        }

        throw new BadRequestException("Not Available");
    }


    @Override
    public Message<CourseUrlDto> getCourseByUrl(String url, Principal principal) throws EntityNotFoundException {
        Boolean isAlreadyBought = false;
        Boolean isEnrolled = false;
        CourseUrl courseUrl = this.courseUrlService.findByUrlAndCourseStatuses(url, Arrays.asList(CourseStatus.PUBLISHED));
        if (Objects.isNull(courseUrl)) {
            throw new EntityNotFoundException("Course not found");
        }
        Course course = this.repo.findByCourseIdAndPublished(courseUrl.getCourse().getId(), CourseStatus.PUBLISHED);
        if (Objects.isNull(course)) {
            throw new EntityNotFoundException("Course not found");
        }

        if (Objects.nonNull(principal)) {
            isEnrolled = this.enrollmentService.isEnrolled(course.getId(), principal.getName());
        }
        if (course.getCourseType().toString().equalsIgnoreCase("PREMIUM_COURSE") && isEnrolled) {
            isAlreadyBought = true;
        }
        return new Message<CourseUrlDto>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Course found.")
                .setData(CourseUrlDto.builder()
                        .activeUrl(courseUrl.getStatus().equals(GenericStatus.ACTIVE) ? courseUrl.getUrl() : this.courseUrlService.findActiveUrlByCourseIdAndStatus(courseUrl.getCourse().getId(), GenericStatus.ACTIVE).getUrl())
                        .canAccess(isEnrolled)
                        .isAlreadyBought(isAlreadyBought)
                        .course(course)
                        .build()
                );
    }

    @Override
    public Message<List<CourseDropdown>> fetchAllCoursesTitleByInstructorForPerformance(String email) throws EntityNotFoundException {
        log.info("Fetching all courses titles via instructor.");
        User user = userService.findByEmail(email);
        List<Tuple> data = repo.findAllByInstructorId(user.getId());

        if (CollectionUtils.isEmpty(data))
            throw new EntityNotFoundException("No data found for this instructor.");

        return new Message<List<CourseDropdown>>()
                .setData(CourseDropdown.from(data))
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Successfully fetched the courses.");
    }

    @Transactional
    public Message<String> changeCourseStatus(Long courseId, String courseStatus, String email) throws EntityNotFoundException, InternalServerException, BadRequestException {
        log.info("Changing course status");
        Long enrolledStudents = 9L;
        Boolean instructorEnrolled = false;

        if (Objects.isNull(courseStatus) || !CourseStatus.isValidCourseStatus(courseStatus)) {
            throw new BadRequestException("Invalid course status");
        }
        if (Objects.isNull(courseId)) {
            throw new BadRequestException("Invalid course ID");
        }

        userService.findByEmail(email);
        Course course = this.repo.findById(courseId).orElseThrow(() -> new EntityNotFoundException("Course not found"));

        if (course.getCourseType().equals(CourseType.PREMIUM_COURSE) &&
                (course.getCourseStatus().equals(CourseStatus.PUBLISHED)) &&
                (CourseStatus.valueOf(courseStatus).equals(CourseStatus.DELETE) ||
                        CourseStatus.valueOf(courseStatus).equals(CourseStatus.UNPUBLISHED) ||
                        CourseStatus.valueOf(courseStatus).equals(CourseStatus.DRAFT))) {
            throw new BadRequestException("Premium course cannot be unpublished, delete, or draft");
        }

        if (course.getCourseStatus() == CourseStatus.PUBLISHED && (CourseStatus.valueOf(courseStatus) == CourseStatus.DELETE) || CourseStatus.valueOf(courseStatus) == CourseStatus.DRAFT) {
            throw new BadRequestException("Course is published");
        }
        if (this.enrollmentService.isEnrolled(courseId, email)) {
            enrolledStudents += 1L;
            instructorEnrolled = true;
        }
        if (course.getCourseStatus() == CourseStatus.PUBLISHED && (CourseStatus.valueOf(courseStatus) == CourseStatus.UNPUBLISHED) && this.enrollmentService.totalNoOfEnrolledStudent(course.getId()) > enrolledStudents) {
            throw new BadRequestException("You cannot unpublished this course because more than " + (instructorEnrolled ? enrolledStudents - 1 : enrolledStudents) + " students are currently enrolled.");
        }
        try {
            com.vinncorp.fast_learner.es_models.Course esCourse = null;
            if (course.getCourseStatus().equals(CourseStatus.PUBLISHED) || course.getCourseStatus().equals(CourseStatus.UNPUBLISHED)) {

//                esCourse = esCourseService.findByDBId(course.getId());
//
//                esCourse.setId(esCourse.getId());
//                esCourse.setDbId(course.getId());
//                esCourse.setTitle(course.getTitle().trim());
//                esCourse.setCourseUrl(this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE).getUrl());
//                esCourse.setDocVector(course.getDocumentVector());
//                esCourse.setThumbnail(course.getThumbnail());
//                esCourse.setCourseStatus(courseStatus);
//                esCourseService.save(esCourse);

                CourseContent courseContent = this.esCourseContentService.getCourseContentById(course.getId().toString());
                courseContent.setStatus(courseStatus);
                this.esCourseContentService.save(Arrays.asList(courseContent));
            }

            course.setCourseStatus(CourseStatus.valueOf(courseStatus.toUpperCase()));
            this.repo.save(course);
            if (CourseStatus.valueOf(courseStatus).equals(CourseStatus.DELETE)) {
                this.courseUrlService.deleteCourseUrlByCourseId(course.getId());
            }

            return new Message<String>()
                    .setData("Successfully changed course status")
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("Successfully changed course status");
        } catch (Exception e) {
            throw new InternalServerException(InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public List<Course> getRelatedCourses(String title, List<Long> tagIds, CourseStatus courseStatus) {
        return repo.findRelatedCourses(title, tagIds, courseStatus);
    }

    public List<Course> findAllByStatuses(List<CourseStatus> courseStatuses) {
        return this.repo.findAllByCourseStatusIn(courseStatuses);
    }

    @Override
    public Message<CourseDetailByPaginatedResponse> findCoursesByFilter(List<String> multipleCategories, String singleCat, String courseType, String search, String feature,
                                                                        double minRating, double maxRating, Long userId, String contentType, Pageable pageable) {
        log.info("Filter parameters - Multiple Categories: {}, Single Category: {}, Course Type: {}, Feature: {}, Rating Range: {}-{}, User ID: {}, Pageable: {}",
                multipleCategories, singleCat, courseType, feature, minRating, maxRating, userId, pageable);

        List<CourseDetailResponse> courseDetailResponse = new ArrayList<>();
        Page<Tuple> data = this.repo.findViewAllBySearchFilter(multipleCategories, singleCat, courseType, search, feature,
                minRating, maxRating, userId, contentType, pageable);
        if (data != null && data.hasContent()) {
            log.info("Successfully fetched courses matching the filter criteria. Total elements: {}, Total pages: {}",
                    data.getTotalElements(), data.getTotalPages());
            courseDetailResponse = data.stream().map(CourseDetailResponse::fromCourseData).collect(Collectors.toList());
            log.info("Mapped {} courses to CourseDetailResponse.", courseDetailResponse.size());
        } else {
            log.warn("No courses found for the requested filter criteria.");
        }
        log.info("Constructing CourseDetailByPaginatedResponse object.");
        CourseDetailByPaginatedResponse response = CourseDetailByPaginatedResponse.builder()
                .data(courseDetailResponse)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .pages(data == null ? 0 : data.getTotalPages())
                .totalElements(data == null ? 0 : data.getTotalElements())
                .build();

// Manually set the parent fields (previousPage, currentPage, nextPage)
        response.setPreviousPage(pageable.getPageNumber() > 0 ? (long) pageable.getPageNumber() - 1 : null);
        response.setCurrentPage((long) pageable.getPageNumber());
        response.setNextPage(pageable.getPageNumber() < (data == null ? 0 : data.getTotalPages()) - 1 ? (long) pageable.getPageNumber() + 1 : null);

        return new Message<CourseDetailByPaginatedResponse>()
                .setData(response)
                .setMessage("Filter courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());

    }

    @Override
    public Message<CourseDetailByPaginatedResponse> getAllNewCourse(Pageable pageable, Principal principal) {
        List<CourseDetailResponse> courseDetailResponse = new ArrayList<>();
        Page<Tuple> data;
        try {
            User user = null;
            if (principal != null) {
                log.info("Fetching user details for principal: {}", principal.getName());
                user = userService.findByEmail(principal.getName());

            }
            Long userId = (user != null) ? user.getId() : null;
            data = repo.findByCreationDateDesc(userId, pageable);
            log.info("Successfully fetched new course data");

            courseDetailResponse = data.stream().map(CourseDetailResponse::fromCourseData).collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Failed to fetch new courses. Reason: {}", e.getMessage());
            return new Message<CourseDetailByPaginatedResponse>()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .setMessage("An error occurred while fetching courses.");
        }
        CourseDetailByPaginatedResponse response = CourseDetailByPaginatedResponse.builder()
                .data(courseDetailResponse)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .pages(data.getTotalPages())
                .totalElements(data.getTotalElements())
                .build();

// Manually set the parent fields (previousPage, currentPage, nextPage)
        response.setPreviousPage(pageable.getPageNumber() > 0 ? (long) pageable.getPageNumber() - 1 : null);
        response.setCurrentPage((long) pageable.getPageNumber());
        response.setNextPage(pageable.getPageNumber() < data.getTotalPages() - 1 ? (long) pageable.getPageNumber() + 1 : null);

        return new Message<CourseDetailByPaginatedResponse>()
                .setData(response)
                .setMessage("All new courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());

    }

    @Override
    public Message<CourseDetailByPaginatedResponse> getAllFreeCourse(Pageable pageable) {
        List<CourseDetailResponse> courseDetailResponse = new ArrayList<>();
        Page<Tuple> data;
        try {
            data = repo.findAllFreeCourses(pageable);
            log.info("Successfully fetched free courses data");

            courseDetailResponse = data.stream().map(CourseDetailResponse::fromCourseData).collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Failed to fetch free courses. Reason: {}", e.getMessage());
            return new Message<CourseDetailByPaginatedResponse>()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .setMessage("An error occurred while fetching free courses.");
        }
        CourseDetailByPaginatedResponse response = CourseDetailByPaginatedResponse.builder()
                .data(courseDetailResponse)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .pages(data.getTotalPages())
                .totalElements(data.getTotalElements())
                .build();
        // Manually set the parent fields (previousPage, currentPage, nextPage)
        response.setPreviousPage(pageable.getPageNumber() > 0 ? (long) pageable.getPageNumber() - 1 : null);
        response.setCurrentPage((long) pageable.getPageNumber());
        response.setNextPage(pageable.getPageNumber() < data.getTotalPages() - 1 ? (long) pageable.getPageNumber() + 1 : null);

        return new Message<CourseDetailByPaginatedResponse>()
                .setData(response)
                .setMessage("All free courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());

    }

    @Override
    public Message<CourseDetailByPaginatedResponse> getAllTrendingCourses(Pageable pageable, Principal principal) {
        List<CourseDetailResponse> courseDetailResponse = new ArrayList<>();
        Page<Tuple> data;
        try {
            User user = null;
            if (principal != null) {
                log.info("Fetching user details for principal: {}", principal.getName());
                user = userService.findByEmail(principal.getName());

            }
            Long userId = (user != null) ? user.getId() : null;

            data = repo.findAllTrendingCourses(userId, pageable);
            if (data != null && data.hasContent()) {
                log.info("Successfully fetched trending courses data");
                courseDetailResponse = data.stream().map(CourseDetailResponse::fromCourseData).collect(Collectors.toList());
            } else {
                log.info("No trending courses found for the requested page.");
            }


        } catch (Exception e) {
            log.warn("Failed to fetch trending courses. Reason: {}", e.getMessage());
            return new Message<CourseDetailByPaginatedResponse>()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .setMessage("An error occurred while fetching trending courses.");
        }


        CourseDetailByPaginatedResponse response = CourseDetailByPaginatedResponse.builder()
                .data(courseDetailResponse)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .pages(data == null ? 0 : data.getTotalPages())
                .totalElements(data == null ? 0 : data.getTotalElements())
                .build();
        // Manually set the parent fields (previousPage, currentPage, nextPage)
        response.setPreviousPage(pageable.getPageNumber() > 0 ? (long) pageable.getPageNumber() - 1 : null);
        response.setCurrentPage((long) pageable.getPageNumber());
        response.setNextPage(pageable.getPageNumber() < (data == null ? 0 : data.getTotalPages()) - 1 ? (long) pageable.getPageNumber() + 1 : null);

        return new Message<CourseDetailByPaginatedResponse>()
                .setData(response)
                .setMessage("All trending courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());


    }

    @Override
    public Message<CourseDetailByPaginatedResponse> getAllPremiumCourses(Pageable pageable, Principal principal) {
        List<CourseDetailResponse> courseDetailResponse = new ArrayList<>();
        Page<Tuple> data;
        try {
            User user = null;
            if (principal != null) {
                log.info("Fetching user details for principal: {}", principal.getName());
                user = userService.findByEmail(principal.getName());

            }
            Long userId = (user != null) ? user.getId() : null;

            data = repo.findPremiumCourses(userId, pageable);
            log.info("Successfully fetched premium courses data");
            if (data != null && data.hasContent()) {
                log.info("Mapping premium courses data");
                courseDetailResponse = data.stream().map(CourseDetailResponse::fromCourseData).collect(Collectors.toList());
            } else {
                log.info("Premium courses not found");
            }

        } catch (Exception e) {
            log.warn("Failed to fetch premium courses. Reason: {}", e.getMessage());
            return new Message<CourseDetailByPaginatedResponse>()
                    .setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setCode(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .setMessage("An error occurred while fetching premium courses.");
        }

        CourseDetailByPaginatedResponse response = CourseDetailByPaginatedResponse.builder()
                .data(courseDetailResponse)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .pages(data == null ? 0 : data.getTotalPages())
                .totalElements(data == null ? 0 : data.getTotalElements())
                .build();
        // Manually set the parent fields (previousPage, currentPage, nextPage)
        response.setPreviousPage(pageable.getPageNumber() > 0 ? (long) pageable.getPageNumber() - 1 : null);
        response.setCurrentPage((long) pageable.getPageNumber());
        response.setNextPage(pageable.getPageNumber() < (data == null ? 0 : data.getTotalPages()) - 1 ? (long) pageable.getPageNumber() + 1 : null);

        return new Message<CourseDetailByPaginatedResponse>()
                .setData(response)
                .setMessage("All premium courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }

    @Override
    public Message<CompletedCourseByPaginated> getCompletedCourse(int pageNo, int pageSize, String email) throws EntityNotFoundException {
        log.info("Fetching completed courses for email: {}", email);

        // Create pagination object
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        // Fetch user
        User user = userService.findByEmail(email);
        if (user == null) {
            log.warn("User not found with email: {}", email);
            throw new EntityNotFoundException("User not found.");
        }
        log.info("User fetched successfully: {}", user);

        // Fetch user profile (optional)
        UserProfile userProfile = userProfileService.getUserProfile(user.getId());
        if (userProfile != null) {
            log.info("User profile fetched successfully for user ID: {}", user.getId());
        } else {
            log.warn("User profile not found for user ID: {}", user.getId());
        }

        // Fetch completed courses
        Page<Tuple> data = repo.getAllCompletedCourseByUser(user.getId(), pageable);
        if (data == null || data.isEmpty()) {
            log.info("No completed courses found for user ID: {}", user.getId());
            return new Message<CompletedCourseByPaginated>()
                    .setData(
                            CompletedCourseByPaginated.builder()
                                    .data(Collections.emptyList()) // Explicitly set an empty ArrayList
                                    .pageNo(pageNo)
                                    .pageSize(pageSize)
                                    .pages(0)
                                    .totalElements(0L)
                                    .build()
                    )
                    .setMessage("No completed courses found.")
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString());
        }

        log.info("Completed courses fetched successfully for user ID: {}", user.getId());

        // Process courses
        List<CompletedCourseResponse> completedCourseResponseList = data.getContent().stream()
                .map(tuple -> {
                    Long courseId = (Long) tuple.get("course_id");
                    Date date = (Date) tuple.get("last_mod_date");

                    // Fetch course details
                    Course course = repo.findById(courseId).orElse(null);
                    if (course == null) {
                        log.warn("Course not found for ID: {}", courseId);
                        return null;
                    }

                    // Fetch active course URL
                    CourseUrl courseUrl;
                    try {
                        courseUrl = courseUrlService.findActiveUrlByCourseIdAndStatus(courseId, GenericStatus.ACTIVE);
                    } catch (EntityNotFoundException e) {
                        log.warn("No active URL found for course ID: {}", courseId);
                        return null;
                    }

                    // Build response
                    return CompletedCourseResponse.builder()
                            .courseId(courseId)
                            .completedCourseDate(date)
                            .userId(user.getId())
                            .courseUrl(courseUrl.getUrl())
                            .creatorName(user.getFullName())
                            .title(course.getTitle())
                            .profilePicture(userProfile != null ? userProfile.getProfilePicture() : null)
                            .courseThumbnailUrl(course.getThumbnail())
                            .build();
                })
                .filter(Objects::nonNull) // Filter out null responses
                .toList();

        if (completedCourseResponseList.isEmpty()) {
            log.info("No courses met the completion criteria for user ID: {}", user.getId());
            return new Message<CompletedCourseByPaginated>()
                    .setData(CompletedCourseByPaginated.builder()
                            .data(Collections.emptyList()) // Explicitly set an empty ArrayList
                            .pageNo(pageNo)
                            .pageSize(pageSize)
                            .pages(0)
                            .totalElements(0L)
                            .build()
                    )
                    .setMessage("No completed courses found.")
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.toString());
        }

        log.info("Completed courses processed successfully for user ID: {}", user.getId());

        // Prepare paginated response
        return new Message<CompletedCourseByPaginated>()
                .setMessage("Completed courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }

    @Override
    public List<Course> findByInstructorIdAndCourseStatus(User instructor, CourseStatus courseStatus) {
        return repo.findByInstructorAndCourseStatus(instructor, courseStatus);
    }

    public List<CourseDetailByType> getCourseDetailByInstructorIdAndType(Long instructorId, CourseType courseType) throws BadRequestException {
        if (Objects.isNull(instructorId) || Objects.isNull(courseType)) {
            throw new BadRequestException("required parameters are missing");
        }
        return this.repo.findByCreatedByAndCourseType(instructorId, courseType);
    }

    public Message<Page<CourseDetailByType>> getPremiumCourses(String search, String name, Pageable pageable) throws EntityNotFoundException {
        User user = this.userService.findByEmail(name);
        Page<CourseDetailByType> data = this.repo.findByCreatedByAndCourseTypeAndSearch(search, user.getId(), CourseType.PREMIUM_COURSE, pageable);
        if (data.getContent().isEmpty()) {
            throw new EntityNotFoundException("No data found");
        }
        return new Message<Page<CourseDetailByType>>()
                .setData(data)
                .setMessage("Instructor premium courses fetched successfully.")
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString());
    }


    @Override
    public List<Course> saveAll(List<Course> course) {
        return repo.saveAll(course);
    }

    @Override
    public Message<SubscriptionPermissionResponse> getPremiumCourseAvailable(String email) throws EntityNotFoundException {
        log.info("Checking if instructor exists with email: {}", email);
        if (email == null) {
            log.warn("Email required");
            return new Message<SubscriptionPermissionResponse>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Email required");
        }
        Optional<User> instructorOpt = userRepository.findByEmail(email);

        if (!instructorOpt.isPresent()) {
            log.warn("Instructor not found with email: {}", email);
            return new Message<SubscriptionPermissionResponse>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setCode(HttpStatus.NOT_FOUND.toString())
                    .setMessage("Instructor not found with email: " + email);
        }

        User instructor = instructorOpt.get();
        log.info("Instructor found with ID: {}", instructor.getId());

        SubscribedUser subscribedUser = subscribedUserService.findByUser(email);
        SubscriptionPermissionResponse subscriptionPermissionResponse;

        if (subscribedUser != null && subscribedUser.getSubscription().getId() != 1) {
            Long subscriptionId = (subscribedUser.getSubscription() != null) ?
                    subscribedUser.getSubscription().getId() : null;

            log.info("Checking subscription validations for subscription ID: {}", subscriptionId);
            SubscriptionValidations subscriptionValidations = subscriptionValidationsService
                    .findByValidationNameAndSubscriptionAndIsActive("PREMIUM_COURSE", subscribedUser.getSubscription(), true);

            if (subscriptionValidations == null) {
                log.warn("No subscription validations found for PREMIUM_COURSE with subscription ID: {}", subscriptionId);
                return new Message<SubscriptionPermissionResponse>()
                        .setStatus(HttpStatus.NOT_FOUND.value())
                        .setCode(HttpStatus.NOT_FOUND.toString())
                        .setMessage("Subscription validation not found for PREMIUM_COURSE.");
            }

            Long countCourse = repo.findByInstructorAndPublished(instructor.getId(), "PUBLISHED");
            log.info("Total published courses for instructor ID {}: {}", instructor.getId(), countCourse);

            Long isAvailableCourse = subscriptionValidations.getValue() - countCourse;
            log.info("Remaining premium courses available: {}", isAvailableCourse);

            subscriptionPermissionResponse = SubscriptionPermissionResponse.builder()
                    .isAvailablePremium(isAvailableCourse > 0)
                    .remainingPremiumCourse(Math.max(isAvailableCourse, 0))
                    .totalPremiumCourse(countCourse)
                    .subscription(subscribedUser.getSubscription())
                    .build();
        } else {
            log.warn("Free subscription found for user with email: {}", email);
            subscriptionPermissionResponse = SubscriptionPermissionResponse.builder()
                    .isAvailablePremium(false)
                    .remainingPremiumCourse(0L)
                    .totalPremiumCourse(0L)
                    .subscription(subscribedUser == null ? null : subscribedUser.getSubscription())
                    .build();
        }

        log.info("Returning subscription permission response for email: {}", email);
        return new Message<SubscriptionPermissionResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.toString())
                .setMessage("Premium course availability fetch successfully.")
                .setData(subscriptionPermissionResponse);

    }
}
