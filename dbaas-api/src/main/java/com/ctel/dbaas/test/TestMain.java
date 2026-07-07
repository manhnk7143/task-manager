package com.ctel.dbaas.test;

import lombok.SneakyThrows;

import java.util.HashMap;

public class TestMain {

    @SneakyThrows
    public static void main(String[] args) {
        // Tạo một HashMap và điển hình hóa dữ liệu
        HashMap<String, String> data = new HashMap<>();
        data.put("mode", "standalone");
        data.put("ipAddr", "10.10.10.20");
        data.put("manhnk", null);

        // Chuyển đổi HashMap thành định dạng section
        String section = convertToSection(data, "redis");

        // In ra kết quả
        System.out.println(section);
    }

    public static String convertToSection(HashMap<String, String> hashMap, String sectionName) {
        StringBuilder sectionBuilder = new StringBuilder();
        sectionBuilder.append("[").append(sectionName).append("]\n");
        for (String key : hashMap.keySet()) {
            String value = hashMap.get(key);
            if (value == null) {
                value = "";
            }
            sectionBuilder.append(key).append("=").append(value).append("\n");
        }

        return sectionBuilder.toString();
    }

}
