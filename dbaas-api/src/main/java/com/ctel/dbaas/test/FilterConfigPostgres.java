package com.ctel.dbaas.test;

import com.ctel.dbaas.init_data.FactoryConfigDatastore;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class FilterConfigPostgres {

    @SneakyThrows
    public static void main(String[] args) {
//        File file = new File("C:\\Users\\CMCTELECOM\\Downloads\\full_config_postgres.txt");
//        File tempDir = FileUtils.getTempDirectory();
//        FileUtils.copyFileToDirectory(file, tempDir);
//        File newTempFile = FileUtils.getFile(tempDir, file.getName());
//        String input = FileUtils.readFileToString(newTempFile,
//                Charset.defaultCharset());
//        JSONArray data = new JSONArray(input);
//        for (int i = 0; i < data.length(); i++) {
//            JSONObject config = data.getJSONObject(i);
//            String name = config.getString("name");
//            double firstVersion = config.getDouble("firstVersion");
//            String last = config.getString("lastVersion");
////            if (!"null".equals(last)) {
////                lastVersion = Double.parseDouble(last);
////            }
//
//            if (firstVersion > 14.0) {
//                System.out.println(name);
//            }
//
//        }

    }

}
