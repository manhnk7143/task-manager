package com.ctel.dbaas.test;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class GenerateRepositoryFile {


    @SneakyThrows
    public static void main(String[] args) {
        String input = "AgentEntity AgentHeartbeatEntity BackupEntity BackupStrategyEntity BaseEntity ComputeEntity ConfigurationEntity DatastoreConfigurationEntity DatastoreEntity DatastoreMode DatastoreVersionEntity GroupConfiguration InstanceEntity NetworkEntity VolumeEntity";
        List<String> fileNames = Arrays.stream(input.split(" ")).map(name -> name.replace("Entity", "") + "Repository").toList();
        for (String fileName : fileNames) {
            List<String> lines = Arrays.asList("package com.ctel.dbaas.repository;", " ", "import org.springframework.data.jpa.repository.JpaRepository;", "", "public interface " + fileName + " extends JpaRepository<"+fileName.replace("Repository", "Entity")+", String> {", "", "}");
            Path path = Files.createFile(Path.of("/home/manhnk/Desktop/work/spring_boot_3/cloudops-dbaas/src/main/java/com/ctel/dbaas/repository/" + fileName + ".java"));
            Files.write(path, lines, StandardCharsets.UTF_8);
        }

    }


}
