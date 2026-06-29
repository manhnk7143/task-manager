/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.main;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.manager.ApplicationManager;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author hieutrinh
 */
@SpringBootApplication
@ComponentScan("com.dev.dbaas")
public class Start {
    
    private static final Logger LOGGER = Logger.getLogger(Start.class);
    
    public static void main(String[] args) {
        
        Config.loadConfig();
        try{
            ApplicationManager.start();
            SpringApplication app = new SpringApplication(Start.class);
            app.run(args);
        } catch (IOException | InterruptedException ex) {
            LOGGER.error(ex, ex);
        }
    }
}
