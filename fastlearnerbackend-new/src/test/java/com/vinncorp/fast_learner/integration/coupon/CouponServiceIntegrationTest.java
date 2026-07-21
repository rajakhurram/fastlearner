package com.vinncorp.fast_learner.integration.coupon;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.coupon.CouponCourse;
import com.vinncorp.fast_learner.models.coupon.CouponEmailDomain;
import com.vinncorp.fast_learner.models.coupon.CouponUser;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.coupon.CouponCourseRepository;
import com.vinncorp.fast_learner.repositories.coupon.CouponEmailDomainRepository;
import com.vinncorp.fast_learner.repositories.coupon.CouponRepository;
import com.vinncorp.fast_learner.repositories.coupon.CouponUserRepository;
import com.vinncorp.fast_learner.request.coupon.CouponRequest;
import com.vinncorp.fast_learner.services.coupon.CouponService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CouponType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CouponServiceIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponEmailDomainRepository couponEmailDomainRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    @Autowired
    private CouponCourseRepository couponCourseRepository;

    private CouponRequest couponRequest;
    private Subscription subscription;
    private Course course;
    private User user;

    @BeforeEach
    void setUp() {
        couponCourseRepository.deleteAll();
        couponEmailDomainRepository.deleteAll();
        couponUserRepository.deleteAll();
        couponRepository.deleteAll();


        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setName("Premium Plan");

        course = new Course();
        course.setId(125L);
        course.setTitle("Java Programming");

        user = new User();
        user.setId(1L);
        user.setEmail("test@vinncorp.com");

        couponRequest = CouponRequest.builder()
                .coupon("TEST123")
                .discount(20.0)
                .startDate(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24))
                .endDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .subscriptionId(2L)
                .couponType(CouponType.SUBSCRIPTION.name())
                .specifiedEmailDomains(List.of("vinncorp.com"))
                .specifiedUsers(List.of("test@vinncorp.com"))
                .specifiedCourses(List.of(125L))
                .isActive(true)
                .build();
    }

    @Test
    void fetchAll_ReturnsCoupons_WhenCouponsExist() throws EntityNotFoundException, InternalServerException {
        // Arrange
        couponService.create(couponRequest);

        // Act
        Message<List<Coupon>> result = couponService.fetchAll();

        // Assert
        assertEquals(200, result.getStatus());
        assertEquals("OK", result.getCode());
        assertFalse(result.getData().isEmpty());
        assertEquals(1, result.getData().size());
        assertEquals("TEST123", result.getData().get(0).getRedeemCode());
    }

    @Test
    void fetchAll_ThrowsEntityNotFoundException_WhenNoCouponsExist() {
        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> couponService.fetchAll());
        assertEquals("No coupons found in the system.", exception.getMessage());
    }

    @Test
    @DisplayName("Create only subscription coupon")
    void createSubscription_SavesCouponWithAssociations_WhenRequestIsValid() throws InternalServerException {
        // Act
        couponRequest.setSubscriptionId(4L);
        couponRequest.setCouponType("SUBSCRIPTION");
        couponRequest.setCoupon("SUBSCRIPTION");
        couponRequest.setSpecifiedUsers(List.of("test@vinncorp.com"));
        couponRequest.setSpecifiedEmailDomains(List.of("vinncorp.com"));

        Message<String> result = couponService.create(couponRequest);

        // Assert
        assertEquals(200, result.getStatus());
        assertEquals("OK", result.getCode());
        assertEquals("Coupon created successfully.", result.getMessage());

        List<Coupon> coupons = couponRepository.findAll();
        assertEquals(1, coupons.size());
        Coupon savedCoupon = coupons.get(0);
        assertEquals("SUBSCRIPTION", savedCoupon.getRedeemCode());
        assertEquals(20.0, savedCoupon.getDiscount());
        assertTrue(savedCoupon.getIsActive());

        List<CouponEmailDomain> domains = couponEmailDomainRepository.findAll();
        assertEquals(1, domains.size());
        assertEquals("vinncorp.com", domains.get(0).getDomain());

        List<CouponUser> users = couponUserRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("test@vinncorp.com", users.get(0).getEmail());
    }

    @Test
    @DisplayName("Create only premium coupon")
    void createPremium_SavesCouponWithAssociations_WhenRequestIsValid() throws InternalServerException {
        // Act
        couponRequest.setCouponType("PREMIUM");
        couponRequest.setCoupon("PREMIUM");
        couponRequest.setSpecifiedUsers(List.of("test@vinncorp.com"));
        couponRequest.setSpecifiedEmailDomains(List.of("vinncorp.com"));
        couponRequest.setSpecifiedCourses(List.of(125L));

        Message<String> result = couponService.create(couponRequest);

        // Assert
        assertEquals(200, result.getStatus());
        assertEquals("OK", result.getCode());
        assertEquals("Coupon created successfully.", result.getMessage());

        List<Coupon> coupons = couponRepository.findAll();
        assertEquals(1, coupons.size());
        Coupon savedCoupon = coupons.get(0);
        assertEquals("PREMIUM", savedCoupon.getRedeemCode());
        assertEquals(20.0, savedCoupon.getDiscount());
        assertTrue(savedCoupon.getIsActive());

        List<CouponEmailDomain> domains = couponEmailDomainRepository.findAll();
        assertEquals(1, domains.size());
        assertEquals("vinncorp.com", domains.get(0).getDomain());

        List<CouponUser> users = couponUserRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("test@vinncorp.com", users.get(0).getEmail());

        List<CouponCourse> courses = couponCourseRepository.findAll();
        assertEquals(1, courses.size());
        assertEquals(125L, courses.get(0).getCourse().getId());
    }

    @Test
    void createBoth_SavesCouponWithAssociations_WhenRequestIsValid() throws InternalServerException {
        // Act
        couponRequest.setSubscriptionId(4L);
        couponRequest.setCouponType("BOTH");
        couponRequest.setCoupon("BOTH");
        couponRequest.setSpecifiedUsers(List.of("test@vinncorp.com"));
        couponRequest.setSpecifiedEmailDomains(List.of("vinncorp.com"));

        Message<String> result = couponService.create(couponRequest);

        // Assert
        assertEquals(200, result.getStatus());
        assertEquals("OK", result.getCode());
        assertEquals("Coupon created successfully.", result.getMessage());

        List<Coupon> coupons = couponRepository.findAll();
        assertEquals(1, coupons.size());
        Coupon savedCoupon = coupons.get(0);
        assertEquals("BOTH", savedCoupon.getRedeemCode());
        assertEquals(true, savedCoupon.isRestricted());
        assertEquals(4L, savedCoupon.getSubscription().getId());
        assertEquals(20.0, savedCoupon.getDiscount());
        assertTrue(savedCoupon.getIsActive());

        List<CouponEmailDomain> domains = couponEmailDomainRepository.findAll();
        assertEquals(1, domains.size());
        assertEquals("vinncorp.com", domains.get(0).getDomain());

        List<CouponUser> users = couponUserRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("test@vinncorp.com", users.get(0).getEmail());

        List<CouponCourse> courses = couponCourseRepository.findAll();
        assertEquals(1, courses.size());
        assertEquals(125L, courses.get(0).getCourse().getId());
    }

    @Test
    void update_UpdatesCoupon_WhenCouponExists() throws EntityNotFoundException, InternalServerException {
        // Arrange
        couponService.create(couponRequest);
        Coupon savedCoupon = couponRepository.findAll().get(0);
        couponRequest.setId(savedCoupon.getId());
        couponRequest.setDiscount(30.0);
        couponRequest.setCoupon("NEWCODE");

        // Act
        Message<String> result = couponService.update(couponRequest);

        // Assert
        assertEquals(200, result.getStatus());
        assertEquals("OK", result.getCode());
        assertEquals("Coupon updated successfully.", result.getMessage());

        Coupon updatedCoupon = couponRepository.findById(savedCoupon.getId()).orElseThrow();
        assertEquals("NEWCODE", updatedCoupon.getRedeemCode());
        assertEquals(30.0, updatedCoupon.getDiscount());
    }

    @Test
    void update_ThrowsEntityNotFoundException_WhenCouponDoesNotExist() {
        // Arrange
        couponRequest.setId(999L);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> couponService.update(couponRequest));
        assertEquals("No coupon found by id.", exception.getMessage());
    }

    @Test
    void delete_RemovesCoupon_WhenCouponExists() throws InternalServerException {
        // Arrange
        couponService.create(couponRequest);
        Coupon savedCoupon = couponRepository.findAll().get(0);

        // Act
        Message<String> result = couponService.delete(savedCoupon.getId());

        // Assert
        assertEquals(200, result.getStatus());
        assertEquals("OK", result.getCode());
        assertEquals("Coupon deleted successfully.", result.getMessage());
        assertFalse(couponRepository.findById(savedCoupon.getId()).isPresent());
    }

    @Test
    @DisplayName(value = "Validate only subscription based coupon.")
    void validateSubscriptionCoupon_ReturnsCoupon_WhenCouponIsValid() throws EntityNotFoundException, InternalServerException {
        // Arrange
        couponRequest.setCouponType(CouponType.SUBSCRIPTION.name());
        couponRequest.setCoupon(CouponType.SUBSCRIPTION.name());
        couponRequest.setSpecifiedUsers(List.of("test@vinncorp.com"));
        couponRequest.setSpecifiedEmailDomains(List.of("yopmail.com"));
        couponRequest.setSubscriptionId(4L);

        couponService.create(couponRequest);

        // Act
        Coupon result = couponService.validateCoupon(couponRequest.getCoupon(), user, CouponType.SUBSCRIPTION, null);

        // Assert
        assertNotNull(result);
        assertEquals(couponRequest.getCoupon(), result.getRedeemCode());
        assertEquals(CouponType.SUBSCRIPTION, result.getCouponType());
    }

    @Test
    @DisplayName(value = "Validate only premium based coupon.")
    void validatePremiumCoupon_ReturnsCoupon_WhenCouponIsValid() throws EntityNotFoundException, InternalServerException {
        // Arrange
        couponRequest.setCouponType(CouponType.PREMIUM.name());
        couponRequest.setCoupon(CouponType.PREMIUM.name());
        couponRequest.setSpecifiedCourses(List.of(125L));
        couponRequest.setSpecifiedUsers(List.of("test@vinncorp.com"));
        couponRequest.setSpecifiedEmailDomains(List.of("yopmail.com"));
        couponRequest.setSubscriptionId(null);

        couponService.create(couponRequest);

        // Act
        Coupon result = couponService.validateCoupon(couponRequest.getCoupon(), user, CouponType.PREMIUM, 125L);

        // Assert
        assertNotNull(result);
        assertEquals(couponRequest.getCoupon(), result.getRedeemCode());
        assertEquals(CouponType.PREMIUM, result.getCouponType());
    }

    @Test
    @DisplayName(value = "Validate both type of for subscription based coupon.")
    void validateBothCouponForSubscription_ReturnsCoupon_WhenCouponIsValid() throws EntityNotFoundException, InternalServerException {
        // Arrange
        couponRequest.setSubscriptionId(4L);
        couponRequest.setCouponType(CouponType.BOTH.name());
        couponRequest.setCoupon(CouponType.BOTH.name());
        couponRequest.setSpecifiedUsers(List.of("test@vinncorp.com"));
        couponRequest.setSpecifiedEmailDomains(List.of("vinncorp.com"));
        couponRequest.setSpecifiedCourses(List.of(125L));

        couponService.create(couponRequest);

        // Act
        Coupon result = couponService.validateCoupon(couponRequest.getCoupon(), user, CouponType.SUBSCRIPTION, null);

        // Assert
        assertNotNull(result);
        assertEquals(couponRequest.getCoupon(), result.getRedeemCode());
        assertEquals(CouponType.BOTH, result.getCouponType());
    }

    @Test
    @DisplayName(value = "Validate both type of for premium coupon.")
    void validateBothCouponForPremium_ReturnsCoupon_WhenCouponIsValid() throws EntityNotFoundException, InternalServerException {
        // Arrange
        couponRequest.setSubscriptionId(4L);
        couponRequest.setCouponType(CouponType.BOTH.name());
        couponRequest.setCoupon(CouponType.BOTH.name());
        couponRequest.setSpecifiedUsers(List.of("test@vinncorp.com"));
        couponRequest.setSpecifiedEmailDomains(List.of("vinncorp.com"));
        couponRequest.setSpecifiedCourses(List.of(125L));

        couponService.create(couponRequest);

        // Act
        Coupon result = couponService.validateCoupon(couponRequest.getCoupon(), user, CouponType.SUBSCRIPTION, 125L);

        // Assert
        assertNotNull(result);
        assertEquals(couponRequest.getCoupon(), result.getRedeemCode());
        assertEquals(CouponType.BOTH, result.getCouponType());
    }

    @Test
    @DisplayName(value = "Validate both type for premium unrestricted coupon.")
    void testValidateCoupon_UnrestrictedCoupon_ShouldPass() throws EntityNotFoundException, InternalServerException {
        couponRequest.setSubscriptionId(4L);
        couponRequest.setCouponType(CouponType.BOTH.name());
        couponRequest.setCoupon(CouponType.BOTH.name());
        couponRequest.setSpecifiedCourses(List.of(125L));

        couponService.create(couponRequest);

        Coupon result = couponService.validateCoupon(couponRequest.getCoupon(), user, CouponType.SUBSCRIPTION, 125L);

        assertNotNull(result);
        assertEquals(result.getRedeemCode(), couponRequest.getCoupon());
    }

    @Test
    void validateCoupon_ThrowsEntityNotFoundException_WhenCouponIsInvalid() {
        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> couponService.validateCoupon("INVALID", user, CouponType.SUBSCRIPTION, null));
        assertEquals("Coupon code is not valid or expired.", exception.getMessage());
    }
}
