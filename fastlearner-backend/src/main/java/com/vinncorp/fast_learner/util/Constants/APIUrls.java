package com.vinncorp.fast_learner.util.Constants;

public class APIUrls {

    private static final String GET_URL = "/";
    private static final String POST_URL = "/";
    private static final String UPDATE_URL = "/update";
    private static final String DELETE_URL = "/";

    // API
    public static final String AUTHENTICATION_MAIN = "/auth";
    public static final String SOCIAL_LOGIN = "/social-login";
    public static final String LOCAL_LOGIN = "/local-login";
    public static final String DO_REFRESH_TOKEN = "/refreshtoken";
    public static final String TOKEN_VALIDATION = "/token-validation";

    public static final String VERIFY_TOKEN = "/verify-token";

    public static final String DO_LOGOUT = "/logout";
    public static final String LOCAL_REGISTER = "/local-register";
    public static final String SENDING_LINK_RESET_PASSWORD = "/send-link";
    public static final String RE_SENDING_LINK_RESET_PASSWORD = "/re-send-link";
    public static final String RESET_PASSWORD = "/reset-password";
    public static final String DISABLE_OR_DELETE_ACCOUNT = "/disable-or-delete";

    public static final String AUTHENTICATION_OTP = "/authentication-otp";

    public static final String VERIFY_AUTHENTICATION_OTP = "/verify-authentication-otp";
    public static final String RESEND_AUTHENTICATION_OTP = "/resend-authentication-otp";

    public static final String VERIFY_OTP = "/verify-otp";

    // API main constant
    public static final String API_MAIN = "/api/v1";

    // API user constants
    public static final String USER_API = API_MAIN + "/user";
    public static final String CHANGE_PASSWORD = "/change-password";

    public static final String GET_USER_DETAILS = "/user-details";


    // API user profile constants
    public static final String USER_PROFILE_API = API_MAIN + "/user-profile";
    public static final String GET_USER_PROFILE_API = GET_URL;
    public static final String USER_PROFILE_UPDATE_API = UPDATE_URL;
    public static final String CREATE_USER_PROFILE_VISIT = POST_URL;

    // API user constants
    public static final String CREATE_USER = POST_URL;
    public static final String DEFINE_ROLE_FOR_A_USER = "/add-role";

    // API course constants
    public static final String COURSE_API = API_MAIN + "/course";
    public static final String GET_ALL_COURSE = GET_URL;
    public static final String CREATE_COURSE = POST_URL + "create";
    public static final String GET_PREMIUM_COURSE_AVAILABLE ="/premium-course-available";

    public static final String GET_COURSE_DETAILS = GET_URL + "get/{courseId}";
    public static final String GET_COURSE_FEEDBACK = GET_URL +  "get/feedback/{courseId}";
    public static final String GET_RELATED_COURSES = "/get-related-courses";
    public static final String SEARCH_BY_FILTER = "/search-by-filter";
    public static final String SEARCH_AUTOCOMPLETE = "/autocomplete";
    public static final String COURSE_SHARED = "/course-shared";
    public static final String GET_COURSE_DETAILS_FOR_UPDATE_FIRST_STEP = "/course-detail-for-update-first-step/{courseId}";
    public static final String UNIQUE_COURSE_TITLE = "/unique-course-title";
    public static final String UNIQUE_COURSE_URL = "/unique-course-url";
    public static final String COURSE_URL = "/course-url";
    public static final String COURSE_TITLE_DROPDOWN_FOR_PERFORMANCE_PAGE = "/dropdown-for-performance";
    public static final String GET_ALL_COURSES_BY_CATEGORY = "/course-by-category";
    public static final String GET_ALL_COURSES_BY_INSTRUCTOR_FOR_PROFILE = "/course-by-teacher-for-profile";
    public static final String GET_COURSES_BY_TEACHER = "/course-by-teacher";
    public static final String CHANGE_COURSE_STATUS = "/course-status";
    public static final String ES_COURSE_SEARCH = "/course-search";

    public static final String COMPLETED_COURSE = "/completed-course";
    public static final String DUMB_DB_COURSE_TO_ES = "/dumb-db-course-to-es";
    public static final String PREMIUM_COURSES = "/premium-courses";

    // API course category constants
    public static final String COURSE_CATEGORY_API = API_MAIN + "/course-category";
    public static final String GET_ALL_COURSE_CATEGORY = GET_URL;

    // API course level constants
    public static final String COURSE_LEVEL_API = API_MAIN + "/course-level";
    public static final String GET_ALL_COURSE_LEVEL = GET_URL;

    public static final String PREMIUM_STUDENTS = API_MAIN + "/premium-students";
    public static final String GET_PREMIUM_STUDENTS = GET_URL;

    public static final String GET_PREMIUM_STUDENTS_BY_DATE ="/by-date";

    public static final String GET_PREMIUM_STUDENTS_EXPORT = "/export/premium-students";


    // API course review constants
    public static final String COURSE_REVIEW_API = API_MAIN + "/course-review";
    public static final String CREATE_COURSE_REVIEW = POST_URL;
    public static final String GET_COURSE_REVIEW = GET_URL + "{courseId}";
    public static final String GET_ALL_REVIEWS_FOR_COURSE = GET_URL;
    public static final String GET_ALL_REVIEWS_FOR_INSTRUCTOR = "/instructor";
    public static final String LIKE_DISLIKE_A_REVIEW = "/like/{courseReviewId}/{status}";

    // subscription permission api url
    public static final String SUBSCRIPTION_PERMISSION = API_MAIN + "/subscription-permission";

    public static final String CREATE_SUBSCRIPTION_PERMISSION ="/create-subscription-permission";

    // API subscription constants
    public static final String SUBSCRIPTION_API = API_MAIN + "/subscription";
    public static final String GET_ALL_SUBSCRIPTION = GET_URL;
    public static final String GET_SUBSCRIPTION_BY_ID = GET_URL + "{subscriptionId}";
    public static final String GET_CURRENT_SUBSCRIPTION = "/current-subscription";
    public static final String CANCEL_SUBSCRIPTION = "/cancel-subscription";
    public static final String VERIFY_SUBSCRIPTION = "/verify";

    // API topic type constants
    public static final String TOPIC_TYPE_API = API_MAIN + "/topic-type";
    public static final String GET_ALL_TOPIC_TYPE = GET_URL;

    // API tag type constants
    public static final String TAG_API = API_MAIN + "/tag";
    public static final String GET_ALL_TAGS_BY_NAME = GET_URL;
    public static final String GET_ALL_TAGS_BY_COURSE = "/{courseId}";

    // API tag type constants
    public static final String FAVOURITE_COURSE_API = API_MAIN + "/favourite-course";
    public static final String CREATE_FAVOURITE_COURSE = POST_URL;
    public static final String GET_FAVOURITE_COURSES = GET_URL;

    // API enrollment constants
    public static final String ENROLLMENT = API_MAIN + "/enrollment";
    public static final String GET_ENROLLMENT = GET_URL;
    public static final String CREATE_ENROLLMENT = POST_URL;

    // API section constants
    public static final String SECTION = API_MAIN + "/section";
    public static final String GET_SECTION = GET_URL + "{courseId}";
    public static final String GET_SECTION_FOR_UPDATE = "/section-for-update/{courseId}";

    // API user course progress constants
    public static final String USER_COURSE_PROGRESS = API_MAIN + "/user-course-progress";
    public static final String CREATE_COURSE_PROGRESS = POST_URL;
    public static final String GET_COURSE_PROGRESS = GET_URL + "{courseId}";
    public static final String GET_ALL_ACTIVE_STUDENTS = "/active-students";

    // API topic constants
    public static final String TOPIC = API_MAIN + "/topic";
    public static final String GET_ALL_TOPIC_BY_COURSE_AND_SECTION = GET_URL + "course/{courseId}/section/{sectionId}";
    public static final String GET_SUMMARY = "/summary/{topicId}";
    public static final String CREATE_TOPIC_PROGRESS = "/progress";
    public static final String GET_ALL_TOPIC_BY_SECTION_FOR_UPDATE = "/section/{sectionId}";

    // API section review constants
    public static final String SECTION_REVIEW = API_MAIN + "/section-review";
    public static final String CREATE_SECTION_REVIEW = POST_URL;
    public static final String GET_SECTION_REVIEW = GET_URL+"{sectionId}";

    // API topic notes constants
    public static final String TOPIC_NOTES = API_MAIN + "/topic-notes";
    public static final String CREATE_TOPIC_NOTES = POST_URL;
    public static final String GET_ALL_TOPIC_NOTES = GET_URL;
    public static final String DELETE_TOPIC_NOTES = DELETE_URL;

    // API uploader constants
    public static final String UPLOADER = API_MAIN + "/uploader";
    public static final String UPLOAD = POST_URL;
    public static final String UPLOAD_FOR_REGENERATION = "/regenerate";
    public static final String DELETE_RESOURCE = DELETE_URL;

    // API downloader constants
    public static final String DOWNLOADER = API_MAIN + "/downloader";
    public static final String DOWNLOAD = GET_URL;

    // API question constants
    public static final String QUESTION = API_MAIN + "/question";
    public static final String CREATE_QUESTION = POST_URL;
    public static final String GET_QUESTIONS = GET_URL;

    // API answer constants
    public static final String ANSWER = API_MAIN + "/answer";
    public static final String CREATE_ANSWER = POST_URL;
    public static final String GET_ANSWERS = GET_URL;

    // API chat constants
    public static final String CHAT = API_MAIN + "/chat";
    public static final String GET_CHAT_CONTENTS = GET_URL;
    public static final String SEND_QUESTION = POST_URL;
    public static final String DELETE_CHAT = "/delete";

    // API chat history constants
    public static final String CHAT_HISTORY = API_MAIN + "/chat-history";
    public static final String GET_CHAT_HISTORY_BY_CHAT = GET_URL + "{chatId}";
    public static final String GET_CHAT_HISTORY_BY_VIDEO = GET_URL + "video/{videoId}";

    // API alternate topic constants
    public static final String ALTERNATE_TOPIC = API_MAIN + "/alternate-topic";
    public static final String GET_ALL_ALTERNATE_TOPICS = GET_URL;
    public static final String PIN_ALTERNATE_TOPIC = POST_URL;
    public static final String UNPIN_ALTERNATE_TOPIC = POST_URL;

    // API alternate section constants
    public static final String ALTERNATE_SECTION = API_MAIN + "/alternate-section";
    public static final String GET_ALL_ALTERNATE_SECTIONS = GET_URL;
    public static final String PIN_ALTERNATE_SECTION = POST_URL;
    public static final String UNPIN_ALTERNATE_SECTION = POST_URL;

    public static final String QUIZ = API_MAIN + "/quiz";
    public static final String VALIDATE_ANSWER = POST_URL + "validate-answer";

    public static final String VALIDATE_ANSWERS = POST_URL + "validate-answering";


    public static final String AI_GENERATOR = "/api/v1/ai-generator";
    public static final String GET_TOPIC_OR_ARTICLE = "/";
    public static final String GET_KEYWORDS = "/keywords";

    public static final String REGENERATE_SUMMARY = "/regenerate-summary";

    public static final String DASHBOARD = "/api/v1/dashboard";
    public static final String FETCH_DASHBOARD_STATS = "/stats";

    public static final String PERFORMANCE ="/api/v1/performance";
    public static final String GET_PROFILE_VISIT = "/profile-visits";

//Payment api urls
    public static final String PAYMENT_GATEWAY= API_MAIN +"/authorizenet";
    public static final String CREATE_SUBSCRIPTION = "/create-subscription";
    public static final String FREE_SUBSCRIPTION_FOR_SIGNUP = "/free-subscription-for-signup";
    public static final String UPDATE_SUBSCRIPTION = "/update-subscription";
    public static final String PAYMENT_PROFILE = "/payment-profile";
    public static final String GET_SAVED_PAYMENT_PROFILE = PAYMENT_PROFILE + "/saved";
    public static final String UPDATE_SAVED_PAYMENT_PROFILE_STATUS = PAYMENT_PROFILE + "/saved";

    public static final String GET_ALL_PAYMENT_PROFILE = PAYMENT_PROFILE + GET_URL;
    public static final String UPDATE_PAYMENT_PROFILE = PAYMENT_PROFILE + UPDATE_URL;
    public static final String ADD_PAYMENT_PROFILE = PAYMENT_PROFILE + "/create";
    public static final String DELETE_PAYMENT_PROFILE = PAYMENT_PROFILE + DELETE_URL + "{profileId}";
    public static final String GET_PAYMENT_PROFILE = PAYMENT_PROFILE + GET_URL + "get/{profileId}";

    //payment checkout api urls
    public static final String COURSE_CHECKOUT_API = API_MAIN +"/checkout";
    public static final String CHARGE_PAYMENT = "/";
    public static final String GET_ALL_PAYOUT_HISTORY = "/payout-history";

    // payment webhook api urls
    public static final String PAYMENT_GATEWAY_WEBHOOK = API_MAIN +"/authorizenet-webhook";
    public static final String PAYMENT_GATEWAY_SUBSCRIPTION_TERMINATION_URL = "/subscription-termination";
    public static final String PAYMENT_GATEWAY_SUBSCRIPTION_PAYMENT = "/subscription-payment";
    public static final String PAYMENT_GATEWAY_WEBHOOK_LOG = "/logs";

    public static final String HISTORY = "/history";
    public static final String GET_BILLING_HISTORY = HISTORY + GET_URL;

    public static final String GET_INVOICE_DETAIL = HISTORY + "/detail";

    public static final String NOTIFICATION = "/api/v1/notification";
    public static final String FETCH_ALL_NOTIFICATION_WITH_PAGINATION = "/fetch-all";
    public static final String FETCH_NOTIFICATION = "/register/{timestamp}";
    public static final String DELETE_NOTIFICATION = DELETE_URL;
    public static final String DELETE_ALL_NOTIFICATION_BY_USER = "/delete-all";

    public static final String CERTIFICATE = "/api/v1/certificate";
    public static final String GET_CERTIFICATE = "/generate/{courseId}";
    public static final String VERIFY_CERTIFICATE = "/verify/{certificateId}";
    public static final String VERIFY_CERTIFICATE_FOR_RESPONSE = "/verify/to/{certificateId}";
    public static final String DOWNLOAD_CERTIFICATE = "/download";
    public static final String CONTACT_US = "/api/v1/contact-us";
    public static final String SUBMIT = POST_URL;

    public static final String NEWSLETTER_SUBSCRIPTION = API_MAIN + "/newsletter-subscription";
    public static final String SUBSCRIBE_NEWSLETTER = "/subscribe";

    public static final String COURSE_VISITOR = API_MAIN + "/course-visitor";
    public static final String COURSE_VISITOR_GET_ALL = GET_URL;

    public static final String YOUTUBE_VIDEO_DOWNLOADER = API_MAIN + "/youtube-video";
    public static final String YOUTUBE_VIDEO_DOWNLOAD = "/download";
    public static final String YOUTUBE_VIDEO_DURATION = "/duration";

    public static final String STRIPE_ACCOUNT = "/api/v1/stripe-account";
    public static final String STRIPE_ACCOUNT_CREATE = "/";
    public static final String STRIPE_ACCOUNT_DETAILS = "/detail";
    public static final String STRIPE_ACCOUNT_DELETE = "/";
    public static final String STRIPE_ACCOUNT_WITHDRAW = "/";
    public static final String STRIPE_ACCOUNT_HISTORY = "/history";

    // token api urls
    public static final String TOKEN_API = API_MAIN + "/token";
    public static final String DELETE_TOKEN = DELETE_URL;

    // home page api urls

    public static final String HOME_PAGE = API_MAIN + "/home-page";
    public static final String FETCH_NEW_COURSES =  "/new-courses";

    public static final String GET_FREE_COURSES = "/free-courses";
    public static final String GET_PREMIUM_COURSES = "/premium-courses";

    public static final String VIEW_ALL = "/view-all";


    public static final String GET_TOP_INSTRUCTOR = "/top-instructor";

    public static final String GET_TRENDING_COURSES = "/trending-courses";

    public static final String STATIC_PAGE = API_MAIN + "/static-page";
    public static final String GET_STATIC_PAGE = "/{slugify}";

    public static final String CREATE_STATIC_PAGE = POST_URL;

    // Purchased course api url
    public static final String PURCHASED_COURSE = API_MAIN + "/purhcased-course";
    public static final String PURCHASED_COURSE_BY_STUDENT = GET_URL;
    public static final String DOWNLOAD_INVOICE_BY_STUDENT_AND_COURSE = "/download";

    //transaction history
    public static final String TRANSACTIONS_HISTORY = API_MAIN + "/transaction-history";
    public static final String GET_BY_USER ="/get";
    public static final String GET_BY_ID ="/get-by-id";
    public static final String DOWNLOAD_INVOICE ="/download-invoice-by-id";

    //User Session api urls
    public static final String USER_SESSION = API_MAIN + "/user-session";
    public static final String CREATE_SESSION = POST_URL + "create-session-id";
    public static final String USER_SESSION_TOKEN = "/generate-token";


    //affiliate courses api urls

    public static final String AFFILIATE = API_MAIN + "/affiliate";
    public static final String CREATE_AFFILIATE_USER = POST_URL + "create";
    public static final String FETCH_AFFILIATE_USER = GET_URL + "fetch";
    public static final String AFFILIATE_DETAIL_BY_USER_ID = GET_URL + "detail";
    public static final String DELETE_AFFILIATE_USER =  "/delete";
    public static final String UPDATE_AFFILIATE_USER = "/update";
    public static final String STRIPE_RESEND_LINK = GET_URL + "stripe-resend-link";

    public static final String STRIPE_REDIRECT_URL = GET_URL + "stripe-redirect-link";

    //Affiliate Course api urls
    public static final String AFFILIATE_COURSE = API_MAIN + "/affiliate-course";
    public static final String CREATE_AFFILIATE_COURSE = POST_URL;
    public static final String GET_AFFILIATE_COURSES = GET_URL;
    public static final String DELETE_AFFILIATE_COURSE = DELETE_URL;
    public static final String GET_AFFILIATES_BY_COURSE = "/by-course";
    public static final String ASSIGN_COURSE_ACTIVE_INACTIVE = "/course-active-inactive";

    //Instructor Affiliate api urls
    public static final String INSTRUCTOR_AFFILIATE = API_MAIN + "/instructor-affiliate";
    public static final String PREMIUM_COURSES_WITH_AFFILIATE_REWARD = "/premium-courses-with-reward";


    // Payout Watch Time api urls
    public static final String PAYOUT_WATCH_TIME = API_MAIN + "/watch-time";
    public static final String CREATE_PAYOUT_WATCH_TIME = POST_URL;

    // PDF Utility api urls
    public static final String PDF_UTILITY = API_MAIN + "/pdf-util";
    public static final String EXTRACT_DATA = "/";

    public static final String COUPON = API_MAIN + "/coupon";
    public static final String COUPON_CREATE = POST_URL;
    public static final String COUPON_UPDATE = POST_URL;
    public static final String COUPON_DELETE = DELETE_URL;
    public static final String COUPON_FETCH_ALL = GET_URL;

    public static final String COUPON_VALIDATE = "/validate";
}
