package com.vinncorp.fast_learner.mock.purchase_course;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.core.ArrayTuple;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.services.purchase_course.PurchasedCourseService;
import com.vinncorp.fast_learner.services.user.IUserService;
import jakarta.persistence.Tuple;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Disabled;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {PurchasedCourseService.class})
@ExtendWith(MockitoExtension.class)
class PurchasedCourseServiceTest {
    @Mock
    private IEnrollmentService iEnrollmentService;

    @Mock
    private IUserService iUserService;

    @InjectMocks
    private PurchasedCourseService purchasedCourseService;

    @Test
    @DisplayName("Test fetch purchase history for student when return empty data")
    void testFetchPurchaseHistoryForStudent_whenReturnEmptyData() throws EntityNotFoundException {
        when(iEnrollmentService.findAllEnrolledPremiumCoursesOfStudents(anyInt(), anyInt(), Mockito.<Long>any()))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        when(iUserService.findByEmail(Mockito.<String>any())).thenReturn(new User());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            purchasedCourseService.fetchPurchaseHistoryForStudent(1, 3, "jane.doe@example.org");
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("No data found"));
    }

    @Test
    @DisplayName("Test fetch purchase history for student when return single data")
    void testFetchPurchaseHistoryForStudent_whenReturnSingleData() throws EntityNotFoundException {
        Object[] values = new Object[]{1L, "Test course title", 100.0, new Date()};
        String[] aliases = new String[]{"course_id", "title", "price", "enrolled_date"};

        Tuple mockTuple = new ArrayTuple(values, aliases);
        PageImpl<Tuple> pageImpl = new PageImpl<>(List.of(mockTuple));

        when(iEnrollmentService.findAllEnrolledPremiumCoursesOfStudents(anyInt(), anyInt(), Mockito.<Long>any()))
                .thenReturn(pageImpl);
        when(iUserService.findByEmail(Mockito.<String>any())).thenReturn(new User());
        var m = purchasedCourseService.fetchPurchaseHistoryForStudent(1, 3, "jane.doe@example.org");

        assertNotNull(m);
        assertEquals(HttpStatus.OK.value(), m.getStatus());
        assertEquals(1, m.getData().getTotalElements());
    }

    @Test
    @DisplayName("Test fetch purchase history for student when return multiple data")
    void testFetchPurchaseHistoryForStudent_whenReturnMultipleData() throws EntityNotFoundException {
        Object[] value1 = new Object[]{1L, "Test course title", 100.0, new Date()};
        Object[] value2 = new Object[]{2L, "Test course title", 100.0, new Date()};
        String[] aliases = new String[]{"course_id", "title", "price", "enrolled_date"};

        Tuple mockTuple1 = new ArrayTuple(value1, aliases);
        Tuple mockTuple2 = new ArrayTuple(value2, aliases);
        PageImpl<Tuple> pageImpl = new PageImpl<>(List.of(mockTuple1, mockTuple2));
        when(iEnrollmentService.findAllEnrolledPremiumCoursesOfStudents(anyInt(), anyInt(), Mockito.<Long>any()))
                .thenReturn(pageImpl);
        when(iUserService.findByEmail(Mockito.<String>any())).thenReturn(new User());
        var m = purchasedCourseService.fetchPurchaseHistoryForStudent(1, 3, "jane.doe@example.org");

        assertNotNull(m);
        assertEquals(HttpStatus.OK.value(), m.getStatus());
        assertEquals(2, m.getData().getTotalElements());
    }

    @Test
    @DisplayName("Test fetch purchase history for student by course id when return single data")
    void testFetchPurchasedHistoryForStudentByCourseId_whenReturnSingleData() throws EntityNotFoundException {
        Object[] value = new Object[]{"Test course title", "jane.doe@example.org", "Jane Doe", new Date(), 100.0, 10.0, 90.0};
        String[] aliases = new String[]{"title", "email", "full_name", "enrolled_date", "original_price", "discount", "price"};

        Tuple mockTuple = new ArrayTuple(value, aliases);

        when(iEnrollmentService.findAllEnrolledPremiumCoursesOfStudentsByCourseId(Mockito.<Long>any(),
                Mockito.<Long>any())).thenReturn(mockTuple);
        when(iUserService.findByEmail(Mockito.<String>any())).thenReturn(new User());
        var m = purchasedCourseService.fetchPurchasedHistoryForStudentByCourseId(1L, "jane.doe@example.org");

        assertNotNull(m);
        assertEquals(m.getCourseTitle(), "Test course title");
    }

    @Test
    @DisplayName("Test fetch purchase history for student by course id when no data found")
    void testFetchPurchasedHistoryForStudentByCourseId_whenNoDataFound() throws EntityNotFoundException {
        when(iEnrollmentService.findAllEnrolledPremiumCoursesOfStudentsByCourseId(Mockito.<Long>any(),
                Mockito.<Long>any())).thenReturn(null);
        when(iUserService.findByEmail(Mockito.<String>any())).thenReturn(new User());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            purchasedCourseService.fetchPurchasedHistoryForStudentByCourseId(1L, "jane.doe@example.org");
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("No data found"));
    }
}

