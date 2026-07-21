-- Enabling SIMILARITY FUNCTION
CREATE EXTENSION pg_trgm;

---------------------------------------------- INDEXING FASTLEARNER ----------------------------------------------------
CREATE INDEX idx_course_course_status_id ON course(course_status, id);
CREATE INDEX idx_course_created_by_course_status ON course(created_by, course_status);
CREATE INDEX idx_course_course_status_title ON course(course_status, title);
CREATE INDEX idx_course_instructor_course_status ON course(instructor_id, course_status);

CREATE INDEX idx_course_category_id ON course_category(id);

CREATE INDEX idx_users_id ON users(id);

CREATE INDEX idx_course_level_id ON course_level(id);

CREATE INDEX idx_user_profile_created_by ON user_profile(created_by);

CREATE INDEX idx_enrollment_course_student ON enrollment(course_id, student_id);
CREATE INDEX idx_enrollment_course_id ON enrollment(course_id);
CREATE INDEX idx_enrollment_instructor_date ON enrollment (enrolled_date, student_id);

CREATE INDEX idx_favourite_course_course_user ON favourite_course(course_id, created_by);

CREATE INDEX idx_course_review_course ON course_review(course_id);

CREATE INDEX idx_course_visitor_visited_instructor_course ON course_visitor(visited_at, instructor_id, course_id);

CREATE INDEX idx_url ON document(url);

CREATE INDEX idx_user_course_progress ON user_course_progress (course_id, student_id);
CREATE INDEX idx_user_course_progress_student_topic ON user_course_progress (student_id, topic_id, is_completed);
CREATE INDEX idx_ucp_topic_course_student_completion ON user_course_progress (course_id, topic_id, student_id, is_completed);
CREATE INDEX idx_ucp_topic_student ON user_course_progress (topic_id, student_id, is_completed);
CREATE INDEX idx_ucp_student_last_mod ON user_course_progress (student_id, last_mod_date, seek_time);


CREATE INDEX idx_user_course_completion ON user_course_completion (course_id, user_id);

CREATE INDEX idx_email ON authentication_otp(email);
CREATE INDEX idx_otp ON authentication_otp(otp);

CREATE INDEX idx_user_value ON otp(user_id, value);

CREATE INDEX idx_instructor_sales_status_stripe_account_id_creation_date ON instructor_sales (status, stripe_account_id, creation_date);
CREATE INDEX idx_instructor_sales_status_payout_email ON instructor_sales (status, payout_batch_id, stripe_account_id);
CREATE INDEX idx_instructor_sales_instructor_period ON instructor_sales (instructor_id, creation_date);

CREATE INDEX idx_answer_question_user_profile ON answer (question_id, created_by) INCLUDE (answer_text);

CREATE INDEX idx_question_course ON question (id, course_id);
CREATE INDEX idx_question_id ON question (id, topic_id, course_id);

CREATE INDEX idx_quiz_question_answer_quiz_question_id ON quiz_question_anwser (quiz_question_id);

CREATE INDEX idx_section_course_active ON section (course_id, is_active, sequence_number);

CREATE INDEX idx_user_alternate_section_user_course ON user_alternate_section (user_id, course_id);

CREATE INDEX idx_course_tag_course_id_tag_id ON course_tag (course_id, tag_id);

CREATE INDEX idx_subscribed_user_active_subscribed_id_payment_status ON subscribed_user (is_active, subscribed_id, payment_status);

CREATE INDEX idx_subscribed_user_start_date ON subscribed_user (start_date);

CREATE INDEX idx_topic_section_id_topic_type_id ON topic (section_id, topic_type_id);

CREATE INDEX idx_user_profile_visit_id_date ON user_profile_visit (user_profile_id, created_date);

CREATE INDEX idx_video_url ON video (videourl);

------------------------------------------------------------------------------------------------------------------------

--------------------- DATABASE POPULATION ------------------------------

-- ROLE population
INSERT INTO role (id, type) VALUES (1,'STUDENT');
INSERT INTO role (id, type) VALUES (2,'INSTRUCTOR');

-- Course Category population
INSERT INTO course_category (id, name, description, is_active) VALUES (1, 'Development', 'Development related courses.', true);
INSERT INTO course_category (id, name, description, is_active) VALUES (2, 'Algorithm', 'Algorithm related courses.', true);
INSERT INTO course_category (id, name, description, is_active) VALUES (3, 'Design', 'Design related courses.', true);
INSERT INTO course_category (id, name, description, is_active) VALUES (4, 'Web Development', 'Web Development related courses.', true);

-- Course Level population
INSERT INTO course_level (id, name, is_active) VALUES (1, 'Beginner', true);
INSERT INTO course_level (id, name, is_active) VALUES (2, 'Intermediate', true);
INSERT INTO course_level (id, name, is_active) VALUES (3, 'Expert', true);
INSERT INTO course_level (id, name, is_active) VALUES (4, 'All Levels', true);

-- Topic Type population
INSERT INTO topic_type (id, name, is_active) VALUES (1, 'Video', true);
INSERT INTO topic_type (id, name, is_active) VALUES (2, 'Article', true);
INSERT INTO topic_type (id, name, is_active) VALUES (3, 'Animation', true);
INSERT INTO topic_type (id, name, is_active) VALUES (4, 'Quiz', true);

-- Subscription population
INSERT INTO subscription (id, name, description, price, duration, duration_in_word, is_active, paypal_plan_id) VALUES (1, 'Forever', 'This plan will be only applied on free courses', 0.0, 0, 'Forever', true, null);
INSERT INTO subscription (id, name, description, price, duration, duration_in_word, is_active, paypal_plan_id) VALUES (2, 'Standard Plan', 'This plan will be only applied on all courses sections', 15.0, 1, 'Per Month', true, 'P-4ME71791TU018180UMYOUP3I');
INSERT INTO subscription (id, name, description, price, duration, duration_in_word, is_active, paypal_plan_id) VALUES (3, 'Per Month, Billed Annually', 'This plan will be only applied on all courses sections', 10.0, 12, '12 Month', true, 'P-99L19657RL4075517MYOUO5A');

UPDATE course
SET course_url = LOWER(REPLACE(title, ' ', '-'))
WHERE course_url IS NULL and is_active = true;

UPDATE course
SET course_url = LOWER(
                  REPLACE(
                    REGEXP_REPLACE(title, '[^a-zA-Z0-9\s]', '', 'g'),
                    ' ', '-'
                  )
                )
WHERE course_url ~ '[^a-zA-Z0-9-]'
AND is_active = true;


SELECT title, COUNT(*) as title_count
FROM course
WHERE is_active = true
GROUP BY title
HAVING COUNT(*) > 1;


SELECT course_url, COUNT(*) as url_count
FROM course
WHERE course_url IS NOT NULL and is_active = true
GROUP BY course_url
HAVING COUNT(*) > 1;