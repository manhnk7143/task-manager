package com.ctel.dbaas.dto.common;

import com.ctel.dbaas.common.Constant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ResponseDto<T> implements Serializable {

    private T data;

    private int status;

    private Integer error;

    private String msg;

    private LocalDateTime currentTime = LocalDateTime.now();

    public ResponseDto(T data, int status, Integer error, String msg) {
        this.data = data;
        this.status = status;
        this.error = error;
        this.msg = msg;
    }

    public ResponseDto(int status, Integer error, String msg) {
        this.status = status;
        this.error = error;
        this.msg = msg;
    }

    public ResponseDto(T data) {
        this.data = data;
        this.status = 1;
        this.msg = Constant.Message.SUCCESS;
    }

    public ResponseDto(String msg) {
        this.status = 1;
        this.msg = msg;
    }

}
