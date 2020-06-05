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
    private String version;

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

            Project result = new Project(path, projectName);

            String version = (String) configFile.get("version");
            if (version != null) {
                result.setVersion(version);
            } else {
                logger.trace("No project version provided.");
            }

            return result;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        if (version != null) {
            return name.trim() + " - " + version.trim();
        } else {
            return name.trim();
        }
    }
}
