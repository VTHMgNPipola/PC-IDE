package org.vthmgnpipola.pcide.client.lang;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.commons.Language;

public class Project {
    private static final Logger logger = LoggerFactory.getLogger(Project.class);

    @JsonIgnore
    private Path path;

    private String name;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String version;

    private Language language;

    public static Project createProject(Path path) {
        Path projectFile = path.resolve("project.json");
        if (Files.isRegularFile(projectFile)) {
            ObjectMapper mapper = new ObjectMapper();
            Project result = null;
            try {
                result = mapper.readValue(projectFile.toFile(), Project.class);

                if (result.name == null) {
                    logger.warn("No name provided for project on '" + path.toString() + "'!");
                    result = null;
                } else if (result.language == null) {
                    logger.warn("No language provided for project '" + result.name + "'!");
                    result = null;
                }

                // The path of the project isn't in the json file, so I need to set it manually
                assert result != null;
                result.setPath(path);
            } catch (IOException e) {
                logger.error("Error loading project from '" + path.toString() + "'!");
                logger.error(e.getMessage());
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

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public String toString() {
        if (version != null) {
            return name.trim() + " - " + version.trim() + " (" + language.toString() + ")";
        } else {
            return name.trim() + " (" + language.toString() + ")";
        }
    }
}
