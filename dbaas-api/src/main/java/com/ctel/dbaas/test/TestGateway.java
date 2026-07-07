package com.ctel.dbaas.test;

import com.ctel.dbaas.datastore.kafka.model.KafkaCluster;
import com.ctel.dbaas.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class TestGateway {

    public static void main(String[] args) {
//        List<String> lstAz = new ArrayList<>();
//        lstAz.add("AZ1");
//        lstAz.add("AZ2");
//        lstAz.add("AZ3");
//        lstAz.add("AZ4");
//
//        for (int i = 0; i < 10; i++) {
//            System.out.println(lstAz.get(i % lstAz.size()));
//        }

        String json = "{\"zones\":[\"AZ1\",\"AZ2\"]}";
        KafkaCluster cluster = CommonUtils.toObject(json, KafkaCluster.class);
        System.out.println(cluster);
    }

}
