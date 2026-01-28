package com.vinncorp.fast_learner.services.premium_student;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.enrollment.EnrollmentRepository;
import com.vinncorp.fast_learner.response.premium_student.PremiumStudentResponse;
import com.vinncorp.fast_learner.services.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PremiumStudentService implements IPremiumStudentService {

    private final EnrollmentRepository enrollmentRepository;
    private final IUserService userService;


    @Override
    public Message<Page<PremiumStudentResponse>> getPremiumStudentsWithFilters(String email,String search, Date startDate, Date endDate, Pageable pageable) throws EntityNotFoundException, BadRequestException {
            User user = userService.findByEmail(email);
            Page<Tuple> data = enrollmentRepository.findPremiumStudentsWithFilter(user.getId(),search, startDate, endDate, pageable);
            if(!data.getContent().isEmpty()){
                return new Message<Page<PremiumStudentResponse>>()
                        .setData(PremiumStudentResponse.toFrom(data))
                        .setMessage("Premium students fetched successfully.")
                        .setStatus(HttpStatus.OK.value())
                        .setCode(HttpStatus.OK.toString());
            }
            throw new BadRequestException("Data not found");
    }

    public byte[] getPremiumStudentsToExcel(String email, String search, Date startDate, Date endDate, Pageable pageable) throws EntityNotFoundException, IOException {

        User user = userService.findByEmail(email);
        Page<Tuple> data = enrollmentRepository.findPremiumStudentsWithFilter(user.getId(),search, startDate, endDate, pageable);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Premium Students");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Purchase Date");
        headerRow.createCell(2).setCellValue("Student Name ");
        headerRow.createCell(3).setCellValue("Student Email ");
        headerRow.createCell(4).setCellValue("Course Title");

        // Fill data
        int rowNum = 1;
        for (PremiumStudentResponse student : PremiumStudentResponse.toFrom(data).getContent()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getStudentId());

            // Convert purchase date from Date to Excel serial date
            double serialDate = convertDateToExcelSerial(student.getPurchaseDate());
            // Now, you can directly use this serial date in your conversion method if necessary
            Date purchaseDate = convertExcelSerialDate(serialDate);

            // Format the date as needed, for example using SimpleDateFormat
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Adjust format as needed
            String formattedDate = dateFormat.format(purchaseDate);
            row.createCell(1).setCellValue(formattedDate); // Set the formatted date

            row.createCell(2).setCellValue(student.getStudentName());
            row.createCell(3).setCellValue(student.getStudentEmail());
            row.createCell(4).setCellValue(student.getCourseTitle());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();

    }

    public static Date convertExcelSerialDate(double serialDate) {
        long days = (long) serialDate;
        double fraction = serialDate - days;

        LocalDate startDate = LocalDate.of(1900, 1, 1);
        LocalDate actualDate = startDate.plusDays(days - 2);
        LocalDateTime dateTime = actualDate.atStartOfDay().plusHours((long) (fraction * 24));

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static double convertDateToExcelSerial(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        long days = (calendar.getTimeInMillis() - new GregorianCalendar(1900, 0, 1).getTimeInMillis()) / (1000 * 60 * 60 * 24);

        return days + 2;
    }
}
