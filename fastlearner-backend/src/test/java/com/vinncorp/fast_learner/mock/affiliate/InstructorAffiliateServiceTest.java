package com.vinncorp.fast_learner.mock.affiliate;

import com.vinncorp.fast_learner.dtos.affiliate.AffiliatePremiumCourse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.affiliate.InstructorAffiliateRepository;
import com.vinncorp.fast_learner.response.course.CourseDetailByType;
import com.vinncorp.fast_learner.services.affiliate.instructor_affiliate_service.InstructorAffiliateService;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.CourseType;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class InstructorAffiliateServiceTest {

    @Mock
    private InstructorAffiliateRepository repo;
    @Mock
    private ICourseService courseService;
    @Mock
    private IUserService userService;
    @InjectMocks
    private InstructorAffiliateService instructorAffiliateService;

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return InstructorAffiliate when valid instructorId and affiliateId are provided")
    void shouldReturnInstructorAffiliateForValidInput() throws BadRequestException {
        Long instructorId = 1L;
        Long affiliateId = 2L;
        InstructorAffiliate mockAffiliate = new InstructorAffiliate();
        mockAffiliate.setId(1L);
        mockAffiliate.setUsername("test_user");
        when(repo.findActiveAffiliateByInstructorAndAffiliateUser(instructorId, affiliateId, GenericStatus.ACTIVE))
                .thenReturn(mockAffiliate);
        InstructorAffiliate result = instructorAffiliateService.getByInstructorIdAndAffiliateId(instructorId, affiliateId);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("test_user", result.getUsername());
    }

    @Test
    @DisplayName("Should throw BadRequestException when instructorId is null")
    void shouldThrowBadRequestExceptionWhenInstructorIdIsNull() {
        Long instructorId = null;
        Long affiliateId = 2L;
        Assertions.assertThrows(BadRequestException.class, () ->
                instructorAffiliateService.getByInstructorIdAndAffiliateId(instructorId, affiliateId));
        verifyNoInteractions(this.repo);
    }

    @Test
    @DisplayName("Should throw BadRequestException when affiliateId is null")
    void shouldThrowBadRequestExceptionWhenAffiliateIdIsNull() {
        Long instructorId = null;
        Long affiliateId = 2L;
        Assertions.assertThrows(BadRequestException.class, () ->
                instructorAffiliateService.getByInstructorIdAndAffiliateId(instructorId, affiliateId));
        verifyNoInteractions(this.repo);
    }

    @Test
    @DisplayName("Should return premium courses with affiliate reward when valid inputs are provided")
    void shouldReturnPremiumCoursesWithAffiliateRewardForValidInputs() throws EntityNotFoundException, BadRequestException {
        Long affiliateId = 1L;
        String name = "instructor@example.com";
        User user = UserTestData.userData();
        user.setId(1L);
        List<CourseDetailByType> courses = List.of(
                new CourseDetailByType(1L, "Java Basics", "Learn Java", "thumbnail1.jpg","abc", 1L),
                new CourseDetailByType(2L, "Advanced Java", "Master Java", "thumbnail2.jpg","abc", 1L)
        );
        InstructorAffiliate instructorAffiliate = new InstructorAffiliate();
        instructorAffiliate.setDefaultReward(10.0);
        when(userService.findByEmail(name)).thenReturn(user);
        when(courseService.getCourseDetailByInstructorIdAndType(user.getId(), CourseType.PREMIUM_COURSE))
                .thenReturn(courses);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(user.getId(), affiliateId))
                .thenReturn(instructorAffiliate);
        Message<AffiliatePremiumCourse> result = this.instructorAffiliateService.getPremiumCoursesWithAffiliateReward(affiliateId, name);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.OK.value(), result.getStatus());
        Assertions.assertEquals("Instructor premium courses fetched successfully with reward.", result.getMessage());
        Assertions.assertNotNull(result.getData());
        Assertions.assertEquals(10.0, result.getData().getReward());
        Assertions.assertEquals(2, result.getData().getCourseDetailsByType().size());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no premium courses are found")
    void shouldThrowEntityNotFoundExceptionWhenNoCoursesFound() throws EntityNotFoundException, BadRequestException {
        Long affiliateId = 1L;
        String name = "instructor@example.com";
        User user = UserTestData.userData();
        when(userService.findByEmail(name)).thenReturn(user);
        when(courseService.getCourseDetailByInstructorIdAndType(user.getId(), CourseType.PREMIUM_COURSE))
                .thenReturn(Collections.emptyList());
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () ->
                instructorAffiliateService.getPremiumCoursesWithAffiliateReward(affiliateId, name));
        Assertions.assertEquals("No premium courses found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when affiliate is not found")
    void shouldThrowEntityNotFoundExceptionWhenAffiliateNotFound() throws EntityNotFoundException, BadRequestException {
        Long affiliateId = 1L;
        String name = "instructor@example.com";
        User user = UserTestData.userData();
        List<CourseDetailByType> courses = List.of(
                new CourseDetailByType(1L, "Java Basics", "Learn Java", "thumbnail1.jpg","abc", 1L),
                new CourseDetailByType(2L, "Advanced Java", "Master Java", "thumbnail2.jpg","abc", 1L)
        );
        when(userService.findByEmail(name)).thenReturn(user);
        when(courseService.getCourseDetailByInstructorIdAndType(user.getId(), CourseType.PREMIUM_COURSE))
                .thenReturn(courses);
        when(instructorAffiliateService.getByInstructorIdAndAffiliateId(user.getId(), affiliateId))
                .thenReturn(null);
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () ->
                instructorAffiliateService.getPremiumCoursesWithAffiliateReward(affiliateId, name));
        Assertions.assertEquals("Affiliate not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when name is null")
    void shouldBadRequestExceptionWhenNameIsNull() {
        Long affiliateId = 1L;
        String name = null;
        Assertions.assertThrows(BadRequestException.class, () ->
                instructorAffiliateService.getPremiumCoursesWithAffiliateReward(affiliateId, name));
    }

    @Test
    @DisplayName("Should throw BadRequestException when name is null")
    void shouldBadRequestExceptionWhenAffiliateIdIsNull() {
        Long affiliateId = null;
        String name = "instructor@example.com";
        Assertions.assertThrows(BadRequestException.class, () ->
                instructorAffiliateService.getPremiumCoursesWithAffiliateReward(affiliateId, name));
    }
}
