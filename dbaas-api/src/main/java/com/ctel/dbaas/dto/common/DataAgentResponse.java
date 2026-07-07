package com.ctel.dbaas.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataAgentResponse {

    private String messageId = UUID.randomUUID().toString();

    private Long time = System.currentTimeMillis();

    private String clientSocketId;

    private String userId;

    private String data;

}
