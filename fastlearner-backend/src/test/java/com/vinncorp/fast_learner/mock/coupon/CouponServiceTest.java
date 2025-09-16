package com.vinncorp.fast_learner.mock.coupon;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.mock.subscription.SubscriptionTestData;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.coupon.CouponCourseRepository;
import com.vinncorp.fast_learner.repositories.coupon.CouponEmailDomainRepository;
import com.vinncorp.fast_learner.repositories.coupon.CouponRepository;
import com.vinncorp.fast_learner.repositories.coupon.CouponUserRepository;
import com.vinncorp.fast_learner.services.coupon.CouponService;
import com.vinncorp.fast_learner.services.course.CourseService;
import com.vinncorp.fast_learner.services.subscription.ISubscriptionService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CouponType;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {
    @Mock
    private CouponCourseRepository couponCourseRepository;

    @Mock
    private CouponEmailDomainRepository couponEmailDomainRepository;

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponUserRepository couponUserRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private ISubscriptionService iSubscriptionService;

    @Mock
    private IUserService iUserService;

    @Test
    @DisplayName("Test findByCouponCode when provided valid data")
    void testFindByCouponCode_whenProvidedValidData() throws EntityNotFoundException {
        // Arrange
        when(couponRepository.findByRedeemCode(anyString())).thenReturn(Optional.of(CouponTestData.standardSubscriptionCoupon()));
        // Act
        Coupon actualFindByCouponCodeResult = this.couponService.findByCouponCode("STDSUBS10");

        // Assert
        assertNotNull(actualFindByCouponCodeResult);
        assertEquals("STDSUBS10", actualFindByCouponCodeResult.getRedeemCode());
    }

    @Test
    @DisplayName("Test findByCouponCode when provided invalid data")
    void testFindByCouponCode_whenProvidedInvalidData() {
        // Arrange
        when(couponRepository.findByRedeemCode(eq(null))).thenReturn(Optional.empty());
        // Act
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> this.couponService.findByCouponCode(null));

        // Assert
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("No coupon found with coupon code"));
    }

    @Test
    @DisplayName("Test fetchAll for success full multiple coupon")
    void testFetchAll_forSuccessFullMultipleCoupon() throws EntityNotFoundException {

        when(couponRepository.findAll()).thenReturn(
                List.of(
                        CouponTestData.standardSubscriptionCoupon(),
                        CouponTestData.premiumSubscriptionCoupon(),
                        CouponTestData.enterpriseSubscriptionCoupon()
                )
        );

        Message<List<Coupon>> actualFetchAllResult = this.couponService.fetchAll();

        assertNotNull(actualFetchAllResult);
        assertEquals(200, actualFetchAllResult.getStatus());
        assertFalse(actualFetchAllResult.getData().isEmpty());
        assertEquals(3, actualFetchAllResult.getData().size());
    }

    @Test
    @DisplayName("Test create for creating subscription coupon")
    void testCreate_forCreatingSubscriptionCoupon() throws InternalServerException, EntityNotFoundException {
        var request = CouponTestData.couponRequest();
        request.setSpecifiedCourses(null);

        when(iSubscriptionService.findBySubscriptionId(anyLong())).thenReturn(new Message<Subscription>().setData(SubscriptionTestData.standardSubscription()));

        var m = couponService.create(request);

        assertNotNull(m);
        assertEquals(200, m.getStatus());

        verify(couponRepository).save(any(Coupon.class));
        verify(couponUserRepository).saveAll(anyList());
        verify(couponEmailDomainRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Test create for creating premium coupon")
    void testCreate_forCreatingPremiumCoupon() throws InternalServerException, EntityNotFoundException, IOException {
        var request = CouponTestData.couponRequest();
        request.setCouponType(CouponType.PREMIUM.name());
        request.setAllowAllCourse(false);
        request.setCoupon("PREMIUM10");

        when(iSubscriptionService.findBySubscriptionId(anyLong())).thenReturn(new Message<Subscription>().setData(SubscriptionTestData.standardSubscription()));
        when(courseService.findById(anyLong())).thenReturn(CourseTestData.courseData());
        var m = couponService.create(request);

        assertNotNull(m);
        assertEquals(200, m.getStatus());

        verify(couponRepository).save(any(Coupon.class));
        verify(couponUserRepository).saveAll(anyList());
        verify(couponCourseRepository).saveAll(anyList());
        verify(couponEmailDomainRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Test update when provided valid data")
    void testUpdate_whenProvidedValidData() throws EntityNotFoundException, InternalServerException {
        var couponRequest = CouponTestData.couponRequest();
        couponRequest.setId(1L);

        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(CouponTestData.standardSubscriptionCoupon()));

        var coupon =CouponTestData.standardSubscriptionCoupon();
        coupon.setId(1L);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        when(iSubscriptionService.findBySubscriptionId(anyLong())).thenReturn(new Message<Subscription>().setData(SubscriptionTestData.standardSubscription()));

        Message<String> actualUpdateResult = this.couponService.update(couponRequest);

        assertNotNull(actualUpdateResult);
        assertEquals(200, actualUpdateResult.getStatus());
        assertEquals("Coupon updated successfully.", actualUpdateResult.getMessage());
    }

    @Test
    @DisplayName("Test delete when provided valid data")
    void testDelete_whenProvidedValidData() {
        doNothing().when(couponUserRepository).deleteByCouponId(anyLong());
        doNothing().when(couponEmailDomainRepository).deleteByCouponId(anyLong());
        doNothing().when(couponCourseRepository).deleteByCouponId(anyLong());
        doNothing().when(couponRepository).deleteById(anyLong());

        Message<String> actualDeleteResult = this.couponService.delete(1L);

        assertNotNull(actualDeleteResult);
        assertEquals(200, actualDeleteResult.getStatus());
        assertEquals("Coupon deleted successfully.", actualDeleteResult.getMessage());

        verify(couponUserRepository).deleteByCouponId(anyLong());
        verify(couponEmailDomainRepository).deleteByCouponId(anyLong());
        verify(couponCourseRepository).deleteByCouponId(anyLong());
        verify(couponRepository).deleteById(anyLong());
    }

    @Test
    @DisplayName("Test validateCoupon when provided valid data")
    void testValidateCoupon_whenProvidedValidData() throws EntityNotFoundException {
        String couponCode = "STDSUBS10";
        User user = UserTestData.userData();
        CouponType couponType = CouponType.SUBSCRIPTION;
        Long courseId = 1L;

        when(couponRepository.validateCoupon(anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(Optional.of(CouponTestData.standardSubscriptionCoupon()));

        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(CouponTestData.standardSubscriptionCoupon()));

        Coupon actualValidateCouponResult = this.couponService.validateCoupon(couponCode, user, couponType, courseId);

        assertNotNull(actualValidateCouponResult);
        assertEquals("STDSUBS10", actualValidateCouponResult.getRedeemCode());
    }

    @Test
    @DisplayName("Test validateDiscount when provided valid data")
    void testValidateDiscount_whenProvidedValidData() throws BadRequestException, EntityNotFoundException {

        String couponCode = "STDSUBS10";
        String couponType = "SUBSCRIPTION";
        Long courseId = 1L;
        Long subscriptionId = 2L;
        String email = "student@mailinator.com";

        var standardCoupon = CouponTestData.standardSubscriptionCoupon();
        standardCoupon.setId(1L);

        when(iUserService.findByEmail(anyString())).thenReturn(UserTestData.userData());
        when(couponRepository.validateCoupon(anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(Optional.of(standardCoupon));

        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(standardCoupon));

        Message<Coupon> actualValidateDiscountResult = this.couponService.validateDiscount(couponCode, couponType,
                courseId, subscriptionId, email);

        assertNotNull(actualValidateDiscountResult);
        assertEquals(200, actualValidateDiscountResult.getStatus());
        assertEquals("Coupon Fetched successfully.", actualValidateDiscountResult.getMessage());
    }
}

