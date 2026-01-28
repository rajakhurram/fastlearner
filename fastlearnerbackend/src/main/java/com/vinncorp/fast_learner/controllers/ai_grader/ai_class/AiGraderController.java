package com.vinncorp.fast_learner.controllers.ai_grader.ai_class;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.ai_grader.AiGraderClassRequest;
import com.vinncorp.fast_learner.request.ai_grader.ai_result.AIResultReq;
import com.vinncorp.fast_learner.response.ai_grader.ai_class.AIClassPaginatedResp;
import com.vinncorp.fast_learner.response.ai_grader.ai_class.ClassPaginatedResp;
import com.vinncorp.fast_learner.services.ai_grader.IAiGraderClassService;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(APIUrls.AI_GRADER_API)
@RequiredArgsConstructor
public class AiGraderController {

    private final IAiGraderClassService aiGraderClass;
    @GetMapping(APIUrls.GET_CLASS)
    public ResponseEntity<Message> getClass(Principal principal) throws InternalServerException {
        var m = aiGraderClass.getClassByInstructorId(principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.CREATE_CLASS)
    public ResponseEntity<Message> createClass(
            @RequestBody AiGraderClassRequest request,
            Principal principal) throws InternalServerException, EntityNotFoundException {

        var m = aiGraderClass.createClass(request,principal);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.GET_AI_GRADER_CLASS_EXPORT)
    public ResponseEntity<byte[]> getAiGraderClassExport(@RequestBody AIResultReq aiResultReq,@RequestParam int pageNo,
    @RequestParam(defaultValue = "0")@Min(value = 1, message = "Page size must be greater than zero") int pageSize,Principal principal) throws InternalServerException {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        byte[] file = aiGraderClass.getAiGraderClassExport(aiResultReq,pageable, principal);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("assignment_data.xlsx").build());

        return new ResponseEntity<>(file, headers, HttpStatus.OK);
    }


    @PostMapping(APIUrls.GET_ALL_AI_GRADER_CLASS)
    public ResponseEntity<Message<AIClassPaginatedResp>> getAllAIGraderClass(
            @RequestParam (required = false) Long classId, Principal principal, @RequestParam int pageNo,
            @RequestParam(defaultValue = "0")@Min(value = 1, message = "Page size must be greater than zero") int pageSize
    ) throws BadRequestException {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var m = aiGraderClass.getAllClassByInstructor(classId,principal,pageable);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @DeleteMapping(APIUrls.DELETE_AI_GRADER_CLASS)
    public ResponseEntity<Message> deleteAiGraderClass(@RequestParam Long classId) throws BadRequestException, InternalServerException {
        var m = aiGraderClass.deleteClass(classId);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PutMapping(APIUrls.UPDATE_CLASS)
    public ResponseEntity<Message> updateClass(@RequestParam Long classId,@RequestParam String name) throws InternalServerException {
        var m = aiGraderClass.updateClass(classId,name);
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @PostMapping(APIUrls.GET_ALL_AI_GRADER_CLASS_STUDENT)
    public ResponseEntity<Message<ClassPaginatedResp>> getAllAIGraderClassForStudent(
           Principal principal,@RequestParam int pageNo,
            @RequestParam(defaultValue = "0")@Min(value = 1, message = "Page size must be greater than zero") int pageSize
    ) throws BadRequestException, EntityNotFoundException {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var m = aiGraderClass.getAllClassByStudentEmail(principal, pageable);
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
