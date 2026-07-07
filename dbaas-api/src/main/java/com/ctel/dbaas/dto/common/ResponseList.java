package com.ctel.dbaas.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseList<T> {

    private List<T> docs;

    private int page;

    private int size;

    private long total;

    private long totalPage;

}
