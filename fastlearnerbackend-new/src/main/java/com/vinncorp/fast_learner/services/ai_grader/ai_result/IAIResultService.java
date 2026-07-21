package com.vinncorp.fast_learner.services.ai_grader.ai_result;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.ai_grader.ai_result.AIResultReq;
import com.vinncorp.fast_learner.response.ai_grader.ai_result.AIResultResByPaginated;
import com.vinncorp.fast_learner.response.ai_grader.ai_result.AIResultResponse;
import com.vinncorp.fast_learner.response.ai_grader.ai_result_question.AIResultQueRespByPaginated;
import com.vinncorp.fast_learner.response.ai_grader.ai_result_question.AIResultQuestionResponse;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IAIResultService {
    Message<AIResultResByPaginated> getAIResultFilteredByClassIdAndAssessmentId(AIResultReq aiResultReq, Pageable pageable) throws InternalServerException;

    Message<AIResultQueRespByPaginated> getAiResultQuestionsByAIResultId(Long aiResultId, Pageable pageable) throws InternalServerException;

    Message updateAIResultQuestionByAIResultQuestionId(Long aiResultQuestionId, Double score, Double totalMarks) throws InternalServerException;

    Message createManualQuestion(com.vinncorp.fast_learner.request.ai_grader.ai_result_question.CreateManualQuestionRequest request)
            throws InternalServerException;

    Message updateManualQuestion(Long aiResultQuestionId,
            com.vinncorp.fast_learner.request.ai_grader.ai_result_question.UpdateManualQuestionRequest request)
            throws InternalServerException;

    Message deleteManualQuestion(Long aiResultQuestionId) throws InternalServerException;

    Message updateStudentEmailNameRollNumberByAiResultId(Long aiResultId, String email,String studentName,String studentRollNumber) throws InternalServerException;

    Message deleteAIResultByAIResultId(Long aiResultId) throws InternalServerException;

    Message updateStatusByAiResultId(Long aiResultId) throws InternalServerException;

    Message<String> startGrading(List<MultipartFile> quizFiles, MultipartFile answerKeyFile, String evaluationCriteria, Long assessmentId, String email) throws IOException, EntityNotFoundException, BadRequestException, InternalServerException;


    Message sendEmail(String email, Long aiResultId,String name) throws InternalServerException, EntityNotFoundException;

    /** Returns assessmentId and classId in the data map alongside the acceptance message. */
    Message<Map<String, Object>> startGradingLandingPage(List<MultipartFile> quizFiles, MultipartFile answerKeyFile, String evaluationCriteria, Long classId, Long AssesId, String assessmentName, String className, String userSubmittedAnswerAsText, String email) throws IOException, EntityNotFoundException, BadRequestException;

    Message retryGrading(Long resultId) throws BadRequestException, InternalServerException;

    Message getAIResultById(Long id) throws InternalServerException;
}
