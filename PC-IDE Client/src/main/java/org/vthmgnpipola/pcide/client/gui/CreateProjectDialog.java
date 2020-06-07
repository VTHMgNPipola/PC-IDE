package org.vthmgnpipola.pcide.client.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.client.Configuration;
import org.vthmgnpipola.pcide.client.lang.Language;
import org.vthmgnpipola.pcide.client.lang.Project;

public class CreateProjectDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(CreateProjectDialog.class);

    public CreateProjectDialog(ProjectDashboard projectDashboard) {
        super(projectDashboard, Configuration.getInstance().getLanguage().getString("createProject.title"));
        init(projectDashboard);
    }

    private void init(ProjectDashboard projectDashboard) {
        ResourceBundle language = Configuration.getInstance().getLanguage();
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Project name
        contentPane.add(new JLabel(language.getString("createProject.projectName")));
        JTextField projectName = new JTextField();
        contentPane.add(projectName);

        contentPane.add(Box.createVerticalStrut(5));

        // Project language
        contentPane.add(new JLabel(language.getString("createProject.projectLanguage")));
        // For some stupid reason Java don't want me to use #toArray(), so I have to populate an array myself
        List<Language> languageList = Configuration.getInstance().getInterpreter().getAvailableLanguages();
        Language[] languages = new Language[languageList.size()];
        for (int i = 0; i < languageList.size(); i++) {
            languages[i] = languageList.get(i);
        }
        JComboBox<Language> projectLanguage = new JComboBox<>(languages);
        contentPane.add(projectLanguage);

        contentPane.add(Box.createVerticalStrut(5));

        // Save button
        JButton save = new JButton(language.getString("file.save"));
        save.addActionListener(e -> {
            try {
                Path projectPath = Configuration.getInstance().getProjectsPath().resolve(projectName.getText());
                Files.createDirectories(projectPath);

                Project project = new Project();
                project.setPath(projectPath);
                project.setName(projectName.getText());
                project.setLanguage((Language) projectLanguage.getSelectedItem());
                project.setVersion("0.0.1");

                Path projectFile = projectPath.resolve("project.json");

                ObjectMapper mapper = new ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(projectFile.toFile(), project);
            } catch (IOException ioException) {
                logger.error("Unable to create project!");
                logger.error(ioException.getMessage());
            }

            projectDashboard.updateProjectList();
            dispose();
        });
        contentPane.add(save);

        setContentPane(contentPane);
        pack();
    }
}
