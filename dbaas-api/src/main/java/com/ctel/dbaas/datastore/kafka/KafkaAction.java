package com.ctel.dbaas.datastore.kafka;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.instance.InstanceInfo;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.ComputeRepository;
import com.ctel.dbaas.utils.CommonUtils;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class KafkaAction {

    private final Map<String, Object> data;
    private final InstanceInfo instanceInfo;
    private final ComputeRepository computeRepository;

    public KafkaAction(final Map<String, Object> data, final InstanceInfo instanceInfo, ComputeRepository computeRepository) {
        this.data = data;
        this.instanceInfo = instanceInfo;
        this.computeRepository = computeRepository;
    }

    public Map<String, Object> getListTopic() {
        return new HashMap<>();
    }

    public Map<String, Object> createTopic() {
        Map<String, Object> res = new HashMap<>();

        // validate
        if (!topicNameIsValid(this.data.get("topicName"))) {
            throw new AppException(new ErrorResponse("topicName invalid"));
        }

        if (!CommonUtils.isDigits(this.data.get("numPartitions")) || (Integer) this.data.get("numPartitions") < 1) {
            throw new AppException(new ErrorResponse("numPartitions must be greater than 0"));
        }

        int quantityOfBrokers = computeRepository.countByInstanceId(instanceInfo.getInstance().getId());
        log.info("quantityOfBrokers : {}", quantityOfBrokers);
        if (!CommonUtils.isDigits(this.data.get("replicationFactor")) ||
                (Integer) this.data.get("replicationFactor") < 1 ||
                (Integer) this.data.get("replicationFactor") > quantityOfBrokers) {
            throw new AppException(new ErrorResponse("replicationFactor must be greater than equal 1 or less than equal %s", quantityOfBrokers));
        }
        // end validate

        String topicName = (String) this.data.get("topicName");
        Integer numPartitions = (Integer) this.data.get("numPartitions");
        Integer replicationFactor = (Integer) this.data.get("replicationFactor");

        res.put("topicName", topicName);
        res.put("numPartitions", numPartitions);
        res.put("replicationFactor", replicationFactor);

        return res;
    }

    public Map<String, Object> deleteTopic() {
        Map<String, Object> res = new HashMap<>();

        // validate
        if (!topicNameIsValid(this.data.get("topicName"))) {
            throw new AppException(new ErrorResponse("topicName invalid"));
        }
        // end validate
        String topicName = (String) this.data.get("topicName");
        res.put("topicName", topicName);

        return res;
    }

    public Map<String, Object> getListConsumerGroup() {
        return new HashMap<>();
    }

    public Map<String, Object> getMessageByOptions() {
        Map<String, Object> res = new HashMap<>();

        // validate
        if (!topicNameIsValid(this.data.get("topicName"))) {
            throw new AppException(new ErrorResponse("topicName invalid"));
        }

        if (this.data.get("partitions") != null && !CommonUtils.isDigits(this.data.get("partitions"))) {
            throw new AppException(new ErrorResponse("partitions must be numeric"));
        }

        if (this.data.get("limit") != null) {
            if (!CommonUtils.isDigits(this.data.get("limit")) || (Integer) this.data.get("limit") < 50) {
                throw new AppException(new ErrorResponse("limit must be greater than 0"));
            }
        }
//        if (this.data.get("limit") != null && !CommonUtils.isDigits(this.data.get("limit")) || (Integer) this.data.get("limit") < 50) {
//            throw new AppException(new ErrorResponse("limit must be greater than equal 50"));
//        }

        if (this.data.get("timestamp") != null && !CommonUtils.isDigits(this.data.get("timestamp"))) {
            throw new AppException(new ErrorResponse("timestamp must be numeric"));
        }

        // end validate
        String topicName = (String) this.data.get("topicName");
        Integer partitions = this.data.get("partitions") != null ? (Integer) this.data.get("partitions") : null;
        Integer limit = this.data.get("limit") != null ? (Integer) this.data.get("limit") : null;
        Long timestamp = this.data.get("timestamp") != null ? (Long) this.data.get("timestamp") : null;

        res.put("topicName", topicName);
        res.put("partitions", partitions);
        res.put("limit", limit);
        res.put("timestamp", timestamp);

        return res;
    }

    public Map<String, Object> partitionReassignment() {
        Map<String, Object> res = new HashMap<>();

        // validate
        if (!topicNameIsValid(this.data.get("topicName"))) {
            throw new AppException(new ErrorResponse("topicName invalid"));
        }

        if (CommonUtils.isDigits(this.data.get("numPartitions")) ||
                (Integer) this.data.get("numPartitions") < 1) {
            throw new AppException(new ErrorResponse("numPartitions must be greater than 0"));
        }
        // end validate

        String topicName = (String) this.data.get("topicName");
        Integer numPartitions = (Integer) this.data.get("numPartitions");

        res.put("topicName", topicName);
        res.put("numPartitions", numPartitions);

        return res;
    }

    public Map<String, Object> editTopicConfig() {
        Map<String, Object> res = new HashMap<>();

        // validate
        if (!topicNameIsValid(this.data.get("topicName"))) {
            throw new AppException(new ErrorResponse("topicName invalid"));
        }

        if (!CommonUtils.isDigits(this.data.get("retentionMs")) || (Integer) this.data.get("retentionMs") < 0) {
            throw new AppException(new ErrorResponse("retentionMs invalid"));
        }

        if (!CommonUtils.isDigits(this.data.get("partitions"))) {
            throw new AppException(new ErrorResponse("partitions must be numeric"));
        }
        // end validate

        String topicName = (String) this.data.get("topicName");
        Integer retentionMs = (Integer) this.data.get("retentionMs");
        Integer partitions = (Integer) this.data.get("partitions");

        res.put("topicName", topicName);
        res.put("retentionMs", retentionMs);
        res.put("partitions", partitions);

        return res;
    }

//    public Map<String, Object> increasePartition() {
//        Map<String, Object> res = new HashMap<>();
//
//        // validate
//        if (!topicNameIsValid(this.data.get("topicName"))) {
//            throw new AppException(new ErrorResponse("topicName invalid"));
//        }
//
//        if (!CommonUtils.isDigits(this.data.get("partitions"))) {
//            throw new AppException(new ErrorResponse("partitions must be numeric"));
//        }
//        // end validate
//
//        String topicName = (String) this.data.get("topicName");
//        Integer partitions = (Integer) this.data.get("partitions");
//
//        res.put("topicName", topicName);
//        res.put("partitions", partitions);
//
//        return res;
//    }

    public Map<String, Object> publishMessage() {
        Map<String, Object> res = new HashMap<>();

        // validate
        if (!topicNameIsValid(this.data.get("topicName"))) {
            throw new AppException(new ErrorResponse("topicName invalid"));
        }

        if (StringUtils.isBlank((String) this.data.get("message"))) {
            throw new AppException(new ErrorResponse("message cannot be empty"));
        }
        // end validate

        String topicName = (String) this.data.get("topicName");
        String key = (String) this.data.get("key");

        res.put("topicName", topicName);
        res.put("message", this.data.get("message"));
        res.put("key", key);

        return res;
    }

    public Map<String, Object> getConsumerLag() {
        Map<String, Object> res = new HashMap<>();

        // validate
        if (!topicNameIsValid(this.data.get("topicName"))) {
            throw new AppException(new ErrorResponse("topicName invalid"));
        }
        // end validate
        String topicName = (String) this.data.get("topicName");
        res.put("topicName", topicName);

        return res;
    }

    public Map<String, Object> getClusterConfig() {
        return new HashMap<>();
    }

    public Map<String, Object> editClusterConfig() {
        Map<String, Object> req = new HashMap<>();

        // validate
        if (this.data.get("auto.create.topics.enable") == null ||
                !List.of("true", "false").contains(this.data.get("auto.create.topics.enable").toString())) {
            throw new AppException(new ErrorResponse("auto.create.topics.enable invalid"));
        }
        // end validate

        String autoCreateTopicsEnable = (String) this.data.get("auto.create.topics.enable");
        req.put("auto.create.topics.enable", autoCreateTopicsEnable);

        return req;
    }

    private static boolean topicNameIsValid(Object topicNameObj) {
        if (topicNameObj == null) {
            return false;
        }

        String topicName = (String) topicNameObj;
        if (topicName.isEmpty() || topicName.length() > Constant.KAFKA_MAX_TOPIC_NAME_LENGTH) {
            return false;
        }

        if (topicName.startsWith(".") || topicName.startsWith("-") || topicName.endsWith(".") || topicName.endsWith("-")) {
            return false;
        }

        for (char c : topicName.toCharArray()) {
            if (!isValidCharacter(c)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidCharacter(char c) {
        return Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '-';
    }

}
