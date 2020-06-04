package org.vthmgnpipola.pcide.client.gui;

import com.formdev.flatlaf.icons.FlatFileChooserDetailsViewIcon;
import com.formdev.flatlaf.icons.FlatFileChooserNewFolderIcon;
import com.formdev.flatlaf.icons.FlatFileViewDirectoryIcon;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.client.Configuration;
import org.vthmgnpipola.pcide.client.lang.Project;
import org.vthmgnpipola.pcide.client.lang.ServerPseudoCodeInterpreter;

/**
 * The project dashboard is where projects (or tasks, if the client is connected to a server) are shown, and can be
 * selected or removed.
 */
public class ProjectDashboard extends JFrame {
    private Logger logger = LoggerFactory.getLogger(ProjectDashboard.class);

    private DefaultListModel<Project> projectsModel;

    public ProjectDashboard() {
        init();
    }

    private void init() {
        ResourceBundle language = Configuration.getInstance().getLanguage();
        setTitle(language.getString("projectDashboard.title"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setPreferredSize(new Dimension(768, 432));

        // First tab
        projectsModel = new DefaultListModel<>();
        try {
            updateProjectList();
        } catch (IOException e) {
            logger.error("Unable to update project list!");
            logger.error(e.getMessage());
            System.exit(-1);
        }
        JList<Project> projects = new JList<>(projectsModel);
        projects.setLayoutOrientation(JList.VERTICAL);
        projects.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

        // Second tab
        if (Configuration.getInstance().getInterpreter() instanceof ServerPseudoCodeInterpreter) {
            DefaultListModel<String> tasksModel = new DefaultListModel<>();
            JList<String> tasks = new JList<>(tasksModel);

            JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
            tabbedPane.addTab(language.getString("projectDashboard.projects"), new JScrollPane(projects));
            tabbedPane.addTab(language.getString("projectDashboard.tasks"), new JScrollPane(tasks));

            contentPane.add(tabbedPane, BorderLayout.CENTER);
        } else {
            contentPane.add(new JScrollPane(projects), BorderLayout.CENTER);
        }

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton openProject = new JButton(language.getString("file.open"), new FlatFileViewDirectoryIcon());
        openProject.addActionListener(e -> {
            if (!projects.isSelectionEmpty()) {
                logger.info("Opening project '" + projects.getSelectedValue() + "'...");
                new ProjectEditor(projects.getSelectedValue()).setVisible(true);
                dispose();
            }
        });
        bottomPanel.add(openProject);

        JButton newProject = new JButton(language.getString("file.new"), new FlatFileChooserNewFolderIcon());
        bottomPanel.add(newProject);

        JButton settings = new JButton(language.getString("file.settings"), new FlatFileChooserDetailsViewIcon());
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(settings);

        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * By calling this method, the list of projects is cleared and rebuilt.
     * A project is only added to the list if it valid. To a project be considered valid, it must be a folder, and
     * have a {@code project.json} file inside it, that contains at least the following parameters:
     * <ul>
     *     <li>{@code name}: The name of the project, that will be shown in the project list.</li>
     * </ul>
     */
    private void updateProjectList() throws IOException {
        projectsModel.clear();

        Files.list(Configuration.getInstance().getProjectsPath()).forEachOrdered(p -> {
            if (Files.isDirectory(p)) {
                Project project = Project.createProject(p);
                if (project != null) {
                    logger.trace("Found valid project: '" + project.getName() + "'");
                    projectsModel.addElement(project);
                } else {
                    logger.trace("Directory '" + p.getFileName().toString() + "' is not a valid project.");
                }
            }
        });
    }
}
