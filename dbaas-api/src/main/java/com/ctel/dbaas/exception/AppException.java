package com.ctel.dbaas.exception;

import com.ctel.dbaas.dto.common.ErrorResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {

    private ErrorResponse errorResponse;

    public AppException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }

}
