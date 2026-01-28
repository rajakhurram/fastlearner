package com.vinncorp.fast_learner.mock.section;

import com.vinncorp.fast_learner.mock.core.ArrayTuple;
import com.vinncorp.fast_learner.mock.course.CourseTestData;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.util.enums.CourseType;
import jakarta.persistence.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SectionTestData {
    private final static String[] sectionDetailFieldnames = {
            "section_id", "section_name", "section_level", "is_free",
            "topic_id", "topic_name", "duration", "topic_type_name",
            "topic_level", "content", "video_id", "video_filename",
            "video_url", "quiz_id", "quiz_title", "sec_reviews",
            "total_sec_reviews"
    };

    private final static String[] sectionByCourseFieldnames = {"id",
            "name", "sequence_number", "is_free", "topics", "completed",
            "duration",	"sec_reviews", "total_sec_reviews", "course_id", "course_type", "is_enrolled"};

    public static List<Tuple> createSectionByCourseData() {
        List<Tuple> mockSectionByCourseData = new ArrayList<>();

        mockSectionByCourseData.add(createTuple(sectionByCourseFieldnames, 16L, "Introduction to Algebra", 1, true, 2, 1, 1393, 0.5, 2, 12L, CourseType.FREE_COURSE.name(), true));
        mockSectionByCourseData.add(createTuple(sectionByCourseFieldnames, 17L, "History And Quiz", 2, true, 2, 0, 420, 0.0, 0, 12L, CourseType.FREE_COURSE.name(), true));

        return mockSectionByCourseData;
    }

    public static List<Tuple> createSectionDetailData() {

        List<Tuple> mockSectionDetail = new ArrayList<>();

        // Mock data for section 16
        Tuple tuple1 = createTuple(sectionDetailFieldnames,16L, "Introduction to Algebra", 1, true, 30L, "Algebra Basics: What Is Algebra?", 726, "Video", 1, "null", 17L, "Algebra+Basics_+What+Is+Algebra_+-+Math+Antics.mp4", "https://storage.googleapis.com/fastlearner-bucket/VIDEO/Qir3LYXm_Algebra_Basics__What_Is_Algebra__-_Math_Antics.mp4", "null", "null", 0.5, 2);
        Tuple tuple2 = createTuple(sectionDetailFieldnames, 16L, "Introduction to Algebra", 1, true, 31L, "Solving Basic Equations", 667, "Video", 2, "null", 18L, "Algebra+Basics_+Solving+Basic+Equations+Part+1+-+Math+Antics.mp4", "https://storage.googleapis.com/fastlearner-bucket/VIDEO/zUwDWMFv_Algebra_Basics__Solving_Basic_Equations_Part_1_-_Math_Antics.mp4", "null", "null", 0.5, 2);
        mockSectionDetail.add(tuple1);
        mockSectionDetail.add(tuple2);

        // Mock data for section 17
        Tuple tuple3 = createTuple(sectionDetailFieldnames,17L, "History And Quiz", 2, true, 32L, "Problem solving in Egypt and Babylon", 180, "Article", 1, "&lt;h1 style=&quot;text-align: center;&quot;&gt;&lt;strong&gt;Algebra Doucument&lt;/strong&gt;&lt;/h1&gt;\n" +
                "&lt;h1 style=&quot;text-align: center;&quot;&gt;Problem Solving in Egypt and Babylon&lt;/h1&gt;&lt;p style=&quot;text-align: justify&quot;&gt;The earliest extant mathematical text from Egypt is the Rhind papyrus (c. 1650 BC). It and other texts attest to the ability of the ancient Egyptians to solve linear equations in one unknown. A linear equation is a first-degree equation, or one in which all the variables are only to the first power. (In today&rsquo;s notation, such an equation in one unknown would be 7x + 3x = 10.) Evidence from about 300 BC indicates that the Egyptians also knew how to solve problems involving a system of two equations in two unknown quantities, including quadratic (second-degree, or squared unknowns) equations. For example, given that the perimeter of a rectangular plot of land is 100 units and its area is 600 square units, the ancient Egyptians could solve for the field&rsquo;s length l and width w. (In modern notation, they could solve the pair of simultaneous equations 2w + 2l =100 and wl = 600.) However, throughout this period there was no use of symbols&mdash;problems were stated and solved verbally. The following problem is typical:&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Method of calculating a quantity, multiplied by 1 1/2 added 4 it has come to 10. What is the quantity that says it?&lt;/strong&gt;&lt;/p&gt;&lt;img title=&quot;Cousrse Image&quot; src=&quot;http://192.168.18.12:8081/api/v1/downloader/?filename=VeZn9kxv_1299888.png&amp;amp;fileType=ARTICLE&quot; alt=&quot;course-image&quot; &gt;&lt;ul&gt;&lt;li&gt;First, calculate the difference of this 10 to this 4. Then 6 results.&lt;/li&gt;&lt;li&gt;Then, divide 1 by 1 1/2. Then 2/3 results.&lt;/li&gt;&lt;li&gt;Then, calculate 2/3 of this 6. Then 4 results.&lt;/li&gt;&lt;/ul&gt;&lt;p&gt;Behold, it is 4, the quantity that said it. What has been found by you is correct.&lt;/p&gt;&lt;p style=&quot;text-align: justify&quot;&gt;Note that except for 2/3, for which a special symbol existed, the Egyptians expressed all fractional quantities using only unit fractions, that is, fractions bearing the numerator 1. For example, 3/4 would be written as 1/2 + 1/4.&lt;/p&gt;&quot;", "null", "null", "null", "null", "null", 0, 0);
        Tuple tuple4 = createTuple(sectionDetailFieldnames,17L, "History And Quiz", 2, true, 33L, "Basic Algebra Quiz Questions", 240, "Quiz", 2, "null", "null", "null", "null", 9L, "Basic Algebra Quiz Questions", 0, 0);
        mockSectionDetail.add(tuple3);
        mockSectionDetail.add(tuple4);

        return mockSectionDetail;
    }

    private static Tuple createTuple(String[] fieldnames, Object... elements) {
        return new ArrayTuple(elements, fieldnames);
    }

    public static Section sectionData() throws IOException {
        var course = CourseTestData.courseData();
        return Section.builder().id(1L).course(course).name("Section 1").build();
    }
}