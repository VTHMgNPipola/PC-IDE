package org.vthmgnpipola.pcide.client.lang;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Project {
    private static final Logger logger = LoggerFactory.getLogger(Project.class);

    private Path path;
    private String name;

    private Project(Path path, String name) {
        this.path = path;
        this.name = name;
    }

    public static Project createProject(Path path) {
        Path projectFile = path.resolve("project.json");
        if (Files.isRegularFile(projectFile)) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, ?> configFile;
            try {
                configFile = mapper.readValue(projectFile.toFile(), Map.class);
            } catch (IOException e) {
                logger.error("Unable to read project configuration file!");
                logger.error(e.getMessage());
                return null;
            }

            String projectName = (String) configFile.get("name");
            if (projectName == null) {
                logger.warn("Project has no name!");
                return null;
            }

            return new Project(path, projectName);
        }

        return null;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
