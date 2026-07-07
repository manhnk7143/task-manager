package com.ctel.dbaas.dto.common;

import lombok.*;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskManagerRequest {

    private String messageId = UUID.randomUUID().toString();

    private Long time = System.currentTimeMillis();

    private String serviceId;

    private String data;

    @Getter
    private JSONObject jsonData;

    public void decodePacket() throws Exception {
        if (data == null) {
            data = "{}";
        }
        jsonData = new JSONObject(data);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("serviceId", this.serviceId);
        result.put("data", this.data);
        result.put("messageId", this.messageId);
        result.put("time", this.time);

        return result;
    }

}
