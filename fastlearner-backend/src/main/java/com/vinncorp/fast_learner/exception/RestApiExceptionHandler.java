package com.vinncorp.fast_learner.exception;

import com.vinncorp.fast_learner.util.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@ControllerAdvice
public class RestApiExceptionHandler{

    private static final String ERROR = "ERROR: ";

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<Object> handleAuthenticationRequest(AuthenticationException ex){
        Message<Object> m = new Message<>();
        log.error(ERROR + ex.getMessage());
        m.setMessage(ex.getMessage()).setStatus(HttpStatus.UNAUTHORIZED.value()).setCode(HttpStatus.UNAUTHORIZED.toString());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<Object> handleBadRequest(BadRequestException ex){
        Message<Object> m = new Message<>();
        log.error(ERROR + ex.getMessage());
        m.setMessage(ex.getMessage()).setStatus(HttpStatus.BAD_REQUEST.value()).setCode(HttpStatus.BAD_REQUEST.toString());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex){
        Message<Object> m = new Message<>();
        log.error(ERROR + ex.getMessage());
        m.setMessage(ex.getMessage()).setStatus(HttpStatus.NOT_FOUND.value()).setCode(HttpStatus.NOT_FOUND.toString());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @ExceptionHandler(EntityAlreadyExistException.class)
    protected ResponseEntity<Object> handleEntityAlreadyExist(EntityAlreadyExistException ex){
        Message<Object> m = new Message<>();
        log.error(ERROR + ex.getMessage());
        m.setMessage(ex.getMessage()).setStatus(HttpStatus.BAD_REQUEST.value()).setCode(HttpStatus.BAD_REQUEST.toString());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @ExceptionHandler(EntityNotSavedException.class)
    protected ResponseEntity<Object> handleEntityNotSaved(EntityNotSavedException ex){
        Message<Object> m = new Message<>();
        log.error(ERROR + ex.getMessage());
        m.setMessage(ex.getMessage()).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()).setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @ExceptionHandler(EntityNotUpdateException.class)
    protected ResponseEntity<Object> handleEntityNotUpdate(EntityNotUpdateException ex){
        Message<Object> m = new Message<>();
        log.error(ERROR + ex.getMessage());
        m.setMessage(ex.getMessage()).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()).setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        return ResponseEntity.status(m.getStatus()).body(m);
    }


    @ExceptionHandler(InternalServerException.class)
    protected ResponseEntity<Object> handleInternalServer(InternalServerException ex){
        Message<Object> m = new Message<>();
        log.error(ERROR + ex.getMessage());
        m.setMessage(ex.getMessage()).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()).setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected  ResponseEntity<Object> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex){
        Message<Object> m = new Message<>();
        log.error(ERROR + ex.getMessage());
        m.setMessage(ex.getMessage()).setStatus(HttpStatus.BAD_REQUEST.value()).setCode(HttpStatus.BAD_REQUEST.toString());
        return  ResponseEntity.status(m.getStatus()).body(m);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException exception) {
        Message<Object> m = new Message<>();
        log.error(ERROR + exception.getMessage());
        m.setStatus(HttpStatus.BAD_REQUEST.value()).setCode(HttpStatus.BAD_REQUEST.name()).setMessage(exception.getLocalizedMessage());
        return ResponseEntity.status(m.getStatus()).body(m);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        StringBuilder errorMessages = new StringBuilder();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String errorMessage = error.getDefaultMessage();
            errorMessages.append(errorMessage).append(" ");
        });

        Message<Object> m = new Message<>();
        m.setStatus(HttpStatus.BAD_REQUEST.value()).setCode(HttpStatus.BAD_REQUEST.name()).setMessage(errorMessages.toString());
        return new ResponseEntity<>(m, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(CreateCourseValidationException.class)
    public ResponseEntity<Object> handleCreateCourseValidation(CreateCourseValidationException ex) {
        Message<Object> m = new Message<>();
        m.setStatus(HttpStatus.BAD_REQUEST.value())
                .setCode(HttpStatus.BAD_REQUEST.name())
                .setMessage(ex.getMessage())
                .setData(ex.getErrors());

        return new ResponseEntity<>(m, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(LimitExceedException.class)
    protected ResponseEntity<Object> handleEntityLimitExceed(LimitExceedException ex){
        Message<Object> m = new Message<>();
        log.error(ERROR + ex.getMessage());
        m.setMessage(ex.getMessage()).setStatus(HttpStatus.CONFLICT.value()).setCode(HttpStatus.CONFLICT.toString());
        return ResponseEntity.status(m.getStatus()).body(m);
    }
}
