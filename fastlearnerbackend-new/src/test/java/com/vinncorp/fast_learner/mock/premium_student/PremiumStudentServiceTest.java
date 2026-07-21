package com.vinncorp.fast_learner.mock.premium_student;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.mock.core.ArrayTuple;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.enrollment.EnrollmentRepository;
import com.vinncorp.fast_learner.services.premium_student.PremiumStudentService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PremiumStudentServiceTest {
    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private IUserService iUserService;

    @InjectMocks
    private PremiumStudentService premiumStudentService;

    @Test
    @DisplayName("Test getPremiumStudentsWithFilters2 when provides valid data")
    void testGetPremiumStudentsWithFilters2_whenProvidedValidData() throws BadRequestException, EntityNotFoundException {
        Object[] values = new Object[]{1L, "Jane Doe", "jane.doe@example.org", 1L, "English", new Date()};
        String[] aliases = new String[]{"userId", "fullName", "email", "courseId", "courseTitle", "enrolledDate"};

        Tuple mockTuple = new ArrayTuple(values, aliases);
        PageImpl<Tuple> pageImpl = new PageImpl<>(List.of(mockTuple));

        when(enrollmentRepository.findPremiumStudentsWithFilter(
                any(), any(), any(), any(), any())).thenReturn(pageImpl);
        when(iUserService.findByEmail(any())).thenReturn(new User());

        Date start = new Date();
        Date end = new Date();

        Message<?> result = premiumStudentService.getPremiumStudentsWithFilters("jane.doe@example.org", "Search", start, end, null);

        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), result.getStatus());
    }

    @Test
    @DisplayName("Test getPremiumStudentsWithFilters when provides invalid data")
    void testGetPremiumStudentsWithFilters_whenProvidesInvalidData() throws BadRequestException, EntityNotFoundException {
        when(enrollmentRepository.findPremiumStudentsWithFilter(Mockito.<Long>any(), Mockito.<String>any(),
                Mockito.<Date>any(), Mockito.<Date>any(), Mockito.<Pageable>any()))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        when(iUserService.findByEmail(Mockito.<String>any())).thenReturn(new User());
        Date startDate = Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
        assertThrows(BadRequestException.class,
                () -> premiumStudentService.getPremiumStudentsWithFilters("jane.doe@example.org", "Search", startDate,
                        Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()), null));
        verify(enrollmentRepository).findPremiumStudentsWithFilter(Mockito.<Long>any(), Mockito.<String>any(),
                Mockito.<Date>any(), Mockito.<Date>any(), Mockito.<Pageable>any());
        verify(iUserService).findByEmail(Mockito.<String>any());
    }

    @Test
    @DisplayName("Test convertExcelSerialDate when provides valid serial date")
    void testConvertExcelSerialDate_whenProvidesValidSerialDate() {
        Date actualConvertExcelSerialDateResult = PremiumStudentService.convertExcelSerialDate(10.0d);
        assertEquals("1900-01-09", (new SimpleDateFormat("yyyy-MM-dd")).format(actualConvertExcelSerialDateResult));
    }
}

