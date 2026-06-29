/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.utils;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

/**
 *
 * @author hieutrinh
 */
public class FileUtil {

    public static final Logger LOGGER = Logger.getLogger(FileUtil.class);

    public static void createFile(String path, String content, String fileName) throws FileNotFoundException, UnsupportedEncodingException {

        File dir = new File(path);
        boolean exists = dir.exists();
        if (!exists) {
            dir.mkdirs();
        }
        PrintWriter writer = new PrintWriter(path + "/" + fileName, "UTF-8");
        writer.println(content);
        writer.close();
    }

    public static boolean move(String pathFrom, String fromFile, String pathTo, String toFile) {

        LOGGER.info("From : " + pathFrom + "/" + fromFile);
        LOGGER.info("To : " + pathTo + "/" + toFile);

        File fileFrom = new File(pathFrom + "/" + fromFile);
        File fileTo = new File(pathTo + "/" + toFile);
        
        // renaming the file and moving it to a new location 
        if (fileFrom.exists()) {

            LOGGER.info("File from is existed");
            try {
                Files.move(fileFrom, fileTo);
                LOGGER.info("Move success to " + fileTo.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.error(e, e);
                return false;
            }
        } else {
            LOGGER.info("Failed to move the file");
        }
        return true;
    }

    public static void deleteFile(String path, String fileName) {

        File dir = new File(path + "/" + fileName);
        if (dir.exists()) {
            dir.deleteOnExit();
        }
    }

    public static void createFolderIfNotExist(String path) {

        File dir = new File(path);
        boolean exists = dir.exists();
        if (!exists) {
            dir.mkdirs();
        }
    }
}
