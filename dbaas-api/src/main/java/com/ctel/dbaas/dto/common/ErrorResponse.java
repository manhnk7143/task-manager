package com.ctel.dbaas.dto.common;

import com.ctel.dbaas.common.Constant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private int code;

    private String message;

    public ErrorResponse() {

    }

    public ErrorResponse(String message) {
        this.code = Constant.STATUS.ERROR;
        this.message = message;
    }

    public ErrorResponse(String message, Object... objects) {
        this.code = Constant.STATUS.ERROR;
        this.message = String.format(message, objects);
    }

    public ErrorResponse(int code, String message, Object... objects) {
        this.code = code;
        this.message = String.format(message, objects);
    }

    public ErrorResponse(String message, int code) {
        this.code = code;
        this.message = message;
    }

}
