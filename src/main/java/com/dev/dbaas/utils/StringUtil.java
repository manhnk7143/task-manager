/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.utils;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author hieutrinh
 */
public class StringUtil {
    
    private static final Logger LOGGER = Logger.getLogger(StringUtil.class);
    
    public static String removeAgent(String str) {
        try {
            String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll(" ", "-").replaceAll("đ", "d");
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return "";
    }
    public static String getCurrentTimeUsingDate() {
        String result = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return result;
    }
}
