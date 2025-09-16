package com.vinncorp.fast_learner.services.enrollment;

import com.vinncorp.fast_learner.dtos.enrollment.EnrolledCourseDetail;
import com.vinncorp.fast_learner.dtos.enrollment.EnrolledStudentDto;
import com.vinncorp.fast_learner.dtos.user.user_course_progress.CoursesProgress;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.repositories.enrollment.EnrollmentRepository;
import com.vinncorp.fast_learner.services.course.ICourseUrlService;
import com.vinncorp.fast_learner.services.notification.IMilestoneAchievementNotificationService;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.course.CourseUrl;
import com.vinncorp.fast_learner.models.enrollment.Enrollment;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.rabbitmq.RabbitMQProducer;
import com.vinncorp.fast_learner.response.enrollment.EnrolledCourseResponse;
import com.vinncorp.fast_learner.services.course.ICourseService;
import com.vinncorp.fast_learner.services.email_template.IEmailService;

import com.vinncorp.fast_learner.services.user.IUserCourseProgressService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.*;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class EnrollmentService implements IEnrollmentService{

    private EnrollmentRepository repo;

    private final ICourseService courseService;
    private final IUserService userService;
    private final RabbitMQProducer rabbitMQProducer;
    private final IUserCourseProgressService userCourseProgressService;
    private final IMilestoneAchievementNotificationService milestoneAchievementNotificationService;
    private final ICourseUrlService courseUrlService;
    private final IEmailService emailService;
    public EnrollmentService(EnrollmentRepository repo, @Lazy ICourseService courseService, IUserService userService,
                             RabbitMQProducer rabbitMQProducer, @Lazy  IUserCourseProgressService userCourseProgressService,
                             IMilestoneAchievementNotificationService milestoneAchievementNotificationService, ICourseUrlService courseUrlService, IEmailService emailService) {
        this.repo = repo;
        this.courseService = courseService;
        this.userService = userService;
        this.rabbitMQProducer = rabbitMQProducer;
        this.userCourseProgressService = userCourseProgressService;
        this.milestoneAchievementNotificationService = milestoneAchievementNotificationService;
        this.courseUrlService = courseUrlService;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public Message<String> enrolled(Long courseId, String email, boolean requestFromAPI)
            throws EntityNotFoundException, InternalServerException, BadRequestException {
        log.info("Enrolling in course with id: " + courseId);

        User user = userService.findByEmail(email);
        if (repo.existsByStudent_IdAndCourse_Id(user.getId(), courseId)) {
            throw new BadRequestException("Already enrolled in this course.");
        }
        Course course = courseService.findById(courseId);

        if(course.getCourseType() == CourseType.PREMIUM_COURSE && requestFromAPI)
            throw new BadRequestException("This course cannot be enrolled without payment.");

        try {
            repo.save(Enrollment.builder()
                    .enrolledDate(new Date())
                    .course(course)
                    .student(user)
                    .isActive(true)
                    .build());
            CourseUrl courseUrl = this.courseUrlService.findActiveUrlByCourseIdAndStatus(course.getId(), GenericStatus.ACTIVE);
            rabbitMQProducer.sendMessage(course.getTitle(), "student/course-details/"+courseUrl.getUrl(), user.getEmail(), course.getCreatedBy(),
                    course.getContentType(), NotificationContentType.TEXT, NotificationType.ENROLLMENT,
                    course.getId());

            // Notify user about the enrollment
            milestoneAchievementNotificationService.notifyToUserCourseEnrollment(course, courseUrl.getUrl(), user.getId());

            // Send notification to the instructor if enrollment is met the milestone achievement threshold
            long totalEnrolled = totalNoOfEnrolledStudent(course.getId());
            if(milestoneAchievementNotificationService.isMilestoneMet(totalEnrolled))
                milestoneAchievementNotificationService.notifyCourseMilestoneAchievements(totalEnrolled, course, user.getEmail());

            return new Message<String>()
                    .setStatus(HttpStatus.OK.value())
                    .setCode(HttpStatus.OK.name())
                    .setMessage("User "+email+" enrolled in course "+course.getTitle()+ " successfully.")
                    .setData("User "+email+" enrolled in course "+course.getTitle()+ " successfully.");
        } catch (Exception e) {
            throw new InternalServerException("Enrollment " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean isEnrolled(Long courseId, String email) {
        log.info("Checking if the user is enrolled in the course :" + courseId);
        return repo.existsByCourseIdAndStudentEmail(courseId, email);
    }

    @Override
    public Message<EnrolledCourseResponse> getEnrolledCourseByUserId(Integer sortBy, String courseTitle, int pageNo, int pageSize, String email)
            throws EntityNotFoundException, BadRequestException {
        log.info("Fetching my courses.");

        if(Objects.isNull(sortBy)){
            log.error("Sort by parameter is "+sortBy);
            throw new BadRequestException("Sort By parameter should not be null");
        }

        User user = userService.findByEmail(email);

        CourseSortBy type = CourseSortBy.fromValue(sortBy);

        Page<Tuple> pagedData = null;

        switch (type){
            case RECENTLY_ACCESSED, IN_PROGRESS -> {
                Sort s = Sort.by(Sort.Direction.DESC,"last_mode_date");
                pagedData = repo.findAllEnrolledInProgressCoursesByUserId(
                        courseTitle != null ? "%" + courseTitle + "%" : null,
                        user.getId(), false, PageRequest.of(pageNo, pageSize));
            }
            case OLDEST_ACCESSED -> {
                Sort s = Sort.by(Sort.Direction.ASC,"last_mode_date");
                pagedData = repo.findAllEnrolledInProgressCoursesByUserId(
                        courseTitle != null ? "%" + courseTitle + "%" : null,
                        user.getId(), true, PageRequest.of(pageNo, pageSize));
            }
            case COMPLETED -> {
                pagedData = repo.findAllEnrolledCompletedCoursesByUserId(
                        courseTitle != null ? "%" + courseTitle + "%" : null,
                        user.getId(), PageRequest.of(pageNo, pageSize));
            }
            case VIEW_ALL -> {
                pagedData = repo.findAllEnrolledCoursesByUserId(
                        courseTitle != null ? "%" + courseTitle + "%" : null,
                        user.getId(), PageRequest.of(pageNo, pageSize));
            }
        }

        if (pagedData.isEmpty()) {
            throw new EntityNotFoundException("No courses present for the user: "+ email);
        }

        List<EnrolledCourseDetail> enrolledCourseDetail  = EnrolledCourseDetail.from(pagedData);

        List<CoursesProgress> coursesProgresses  = userCourseProgressService.getCoursesProgressByUser(
                enrolledCourseDetail.stream().map(EnrolledCourseDetail::getCourseId).toList(), user.getId());

        List<EnrolledCourseDetail> resp  = enrolledCourseDetail.stream().map(e -> {
            Optional<CoursesProgress> coursesProgress=coursesProgresses.stream()
                    .filter(a->e.getCourseId()==a.getId())
                    .findAny();
            e.setCourseProgress(coursesProgress.isPresent()
            ?(coursesProgress.get().getPercentage()!=null?coursesProgress.get().getPercentage():0)
                    :0);
            return e;
        }).toList();

        EnrolledCourseResponse enrolledCourseResponse = EnrolledCourseResponse.builder()
                .myCourses(resp)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(pagedData.getTotalPages())
                .totalElements(pagedData.getTotalElements())
                .build();

        return new Message<EnrolledCourseResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("My courses fetched successfully.")
                .setData(enrolledCourseResponse);
    }

    @Override
    public EnrolledStudentDto totalNoOfEnrolledStudent(String period, Long instructorId) {
        log.info("Fetching no of total students enrolled in courses by instructor id: " + instructorId);
        List<Tuple> data = repo.findByInstructorId(period, instructorId);
        if(CollectionUtils.isEmpty(data))
            return null;
        return EnrolledStudentDto.from(data);
    }

    @Transactional(readOnly = true)
    @Override
    public long totalNoOfEnrolledStudent(Long courseId) {
        log.info("Fetching no of total students enrolled in a course: " + courseId);
            try {
                return repo.countByCourseId(courseId);
            }catch (Exception e){
                System.out.println(e.getLocalizedMessage());
            }

            return 0;
    }

    @Override
    public List<Long> findRecommendedCoursesIDs(Long courseId) throws EntityNotFoundException {
        log.info("Fetching all recommended courses by course ID: "+ courseId);
        List<Tuple> data = repo.findRecommendedCoursesIDs(courseId);
        if(CollectionUtils.isEmpty(data))
            throw new EntityNotFoundException("No recommendation is found.");
        return data.stream().map(e -> Objects.nonNull(e.get("course_id")) ? (Long) e.get("course_id") : null ).toList();
    }

    public void deleteEnrollmentByCourseIdAndStudentId(Long courseId, String email){
        Enrollment enrollment = repo.findByCourseIdAndStudentEmail(courseId, email);
        if(Objects.nonNull(enrollment)){
            this.repo.deleteById(enrollment.getId());
        }
    }

    @Override
    public Page<Tuple> findAllEnrolledPremiumCoursesOfStudents(int pageNo, int pageSize, Long studentId) throws EntityNotFoundException {
        log.info("Fetching premium courses of students...");
        Page<Tuple> data = repo.findAllPremiumCoursesByStudentId(studentId, PageRequest.of(pageNo, pageSize));
        if(data.isEmpty())
            throw new EntityNotFoundException("No data found.");
        return data;
    }

    @Override
    public Tuple findAllEnrolledPremiumCoursesOfStudentsByCourseId(Long courseId, Long studentId) throws EntityNotFoundException {
        log.info("Fetching premium courses of students with course id...");
        Tuple data = repo.findAllPremiumCoursesByStudentIdAndCourseId(courseId, studentId);
        if(data == null)
            throw new EntityNotFoundException("No data found.");
        return data;
    }


//    @Scheduled(cron = "0 0 12 * * ?")
    public void notifyUserThroughEmail(){
      log.info("Notify email for course enrolled ");
//        List<Tuple> enrollUser=repo.findStudentByEnrollmentDate();

//        if (enrollUser!=null && !enrollUser.isEmpty()) {
//            log.info("Fetched all Students who have not enrolled in 15 days");
//            List<EnrolledCoursesResponse> enrolledCoursesResponses = enrollUser.stream()
//                    .map(e -> EnrolledCoursesResponse.builder()
//                            .studentId((Long) e.get("student_id"))
//                            .fullName((String) e.get("full_Name"))
//                            .email((String) e.get("email"))
//                            .courseId((Long) e.get("course_id"))
//                            .enrolledDate((Date) e.get("enrolled_date"))  // Changed to enrolledDate
//                            .url((String) e.get("url"))
//                            .title((String) e.get("title"))
//                            .isActive((Boolean) e.get("is_active"))
//                            .build()
//                    ).toList();
//            if (!enrolledCoursesResponses.isEmpty()) {
//
//                // Maps to store courses and links for each student
//                Map<Long, List<String>> studentCourses = new HashMap<>();
//                Map<Long, List<String>> studentLinks = new HashMap<>();
//                Map<Long, String> studentNames = new HashMap<>();
//                Map<Long,String> studEmail=new HashMap<>();
//
//                // Populate the maps with course titles, links, and student names
//                for (EnrolledCoursesResponse e : enrolledCoursesResponses) {
//                    Long studentId = e.getStudentId();
//                    String courseName = e.getTitle();
//                    String courseLink = "<p><strong>" + e.getTitle() + "</strong> (last attempted on " + new SimpleDateFormat("yyyy-MM-dd").format(e.getEnrolledDate()) + ")</p>";
//
//                    // Store the student's full name
//                    studentNames.put(studentId, e.getFullName());
//
//                    studEmail.put(studentId,e.getEmail());
//
//                    // Add course name to the map for the student
//                    studentCourses.computeIfAbsent(studentId, k -> new ArrayList<>()).add(courseName);
//
//                    // Add course link to the map for the student
//                    studentLinks.computeIfAbsent(studentId, k -> new ArrayList<>()).add(courseLink);
//                }
//                log.info("Mapped student details with courses and links");
//                // Output the courses and links for each student
//                for (Map.Entry<Long, List<String>> entry : studentCourses.entrySet()) {
//                    Long studentId = entry.getKey();
//                    List<String> courses = entry.getValue();
//                    List<String> links = studentLinks.get(studentId);
//                    String studentName = studentNames.get(studentId);
//                    String StudentEmail= studEmail.get(studentId);
//
//                    // Join course names and links
//                    String courseNames = String.join(", ", courses);
//                    String courseLinks = String.join(" ", links);
//
//
//                    log.info("Preparing email for student ID: {}, Name: {}", studentId, studentName);
//                        // Now, we insert the courseLinks and courseNames into the email template
//                    String body = "<html>" +
//                            "<body>" +
//                            "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
//                            "<tr>" +
//                            "<td align=\"center\">" +
//                            "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border: 1px solid #dddddd; padding: 20px; background-color: #ffffff;\">" +
//                            "<tr>" +
//                            "<td>" +
//                            "<p>Hi <strong>" + studentName + "</strong>,</p>" +
//                            "<p>We hope you're doing well! We’ve been following your progress on FastLearner and are excited to see how far you've come. You’ve been doing an amazing job!</p>" +
//                            "<p>We noticed that you’ve enrolled in the following courses:</p>" +
//                            "<p><strong>" + courseLinks +
//                            "<p>It seems like you’ve taken a break from these courses for a while, but don’t forget—every step you take brings you closer to achieving your goals. Whether you’re ready to pick up where you left off or revisit a previous course, we’re here to support you in getting back on track.</p>" +
//                            "<p>Remember, every small step counts, and we’re thrilled to continue supporting you on your learning journey.</p>" +
//                            "<p>Let’s keep moving forward together!</p>" +
//                            "<p>Best regards,<br>" +
//                            "The FastLearner Team</p>" +
//                            "<p>Log back in now and pick up where you left off:<a href=\"https://fastlearner.ai/\"> https://fastlearner.ai/</a></p>"+
//                            "</td>" +
//                            "</tr>" +
//                            "</table>" +
//                            "</td>" +
//                            "</tr>" +
//                            "</table>" +
//                            "</body>" +
//                            "</html>";
//                        log.info("Sending Email");
//
//                    emailService.sendEmail(StudentEmail, "Your Certificate is waiting for you", body, true);
//                    log.info("Email sent successfully to student ID: {}", studentId);
//
//                };
//            }else {
//                log.info("No students found with pending enrollment over 15 days.");
//            }
//
//        }else {
//            log.info("No students to process.");
//        }


    }

}
