package com.ctel.dbaas;

import com.ctel.dbaas.init_data.InitAgentFirmware;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Log4j2
@SpringBootApplication
@EnableJpaAuditing
public class CloudopsDbaasApplication implements ApplicationRunner {

    @Autowired
    private InitAgentFirmware initAgentFirmware;

    public static void main(String[] args) {
        SpringApplication.run(CloudopsDbaasApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initAgentFirmware.init();
        log.info("Check init data done !");
    }


}
