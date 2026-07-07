package com.ctel.dbaas.exception;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.common.ResponseDto;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@RestControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ErrorResponse responseErr = new ErrorResponse();
        if (ex instanceof AppException) {
            responseErr = (ErrorResponse) body;
        } else if (ex instanceof MissingServletRequestParameterException) {
            responseErr.setCode(Constant.STATUS.ERROR);
            responseErr.setMessage("missing parameter");

//            ExceptionResponse exceptionResponse = new ExceptionResponse(LocalDateTime.now(), "missing parameter", "no detail");
//            return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);

//        } else if (ex instanceof AuthenticationException) {
//            responseErr.setCode(401);
//            responseErr.setMessage("Unauthorized : " + ex.getMessage());
//        } else if (ex instanceof AccessDeniedException) {
//            responseErr.setCode(403);
//            responseErr.setMessage("Access denied : " + ex.getMessage());
        } else {
            responseErr.setCode(Constant.STATUS.ERROR);
            responseErr.setMessage(ex.getMessage());
        }

        ResponseDto<?> responseDto = new ResponseDto<>(Constant.STATUS.ERROR, responseErr.getCode(), responseErr.getMessage());
        return super.handleExceptionInternal(ex, responseDto, headers, statusCode, request);
    }


    @ExceptionHandler({AppException.class})
    public ResponseEntity<?> handleAppException(AppException ex, WebRequest request) {
        ErrorResponse errorResponse = ex.getErrorResponse();
        HttpStatus httpStatus = CommonUtils.getHttpStatus(errorResponse.getCode());
        return handleExceptionInternal(ex, ex.getErrorResponse(), new HttpHeaders(), httpStatus, request);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<?> handleAllException(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }


}
