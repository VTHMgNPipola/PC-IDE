package org.vthmgnpipola.pcide.client.gui;

import com.formdev.flatlaf.icons.FlatFileChooserDetailsViewIcon;
import com.formdev.flatlaf.icons.FlatFileChooserNewFolderIcon;
import com.formdev.flatlaf.icons.FlatFileViewDirectoryIcon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.client.Configuration;
import org.vthmgnpipola.pcide.client.lang.Project;
import org.vthmgnpipola.pcide.client.lang.RabbitMQPseudoCodeInterpreter;
import org.vthmgnpipola.pcide.commons.Language;

/**
 * The project dashboard is where projects (or tasks, if the client is connected to a server) are shown, and can be
 * selected or removed.
 */
public class ProjectDashboard extends JFrame {
    private Logger logger = LoggerFactory.getLogger(ProjectDashboard.class);

    private DefaultListModel<Project> projectsModel;
    private JList<Project> projects;

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
        updateProjectList();
        projects = new JList<>(projectsModel);
        projects.setCellRenderer(new ProjectListCellRenderer());
        projects.setLayoutOrientation(JList.VERTICAL);
        projects.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        projects.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F5) {
                    updateProjectList();
                }
            }
        });
        projects.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1) {
                    openSelectedProject();
                }
            }
        });

        // Second tab
        // TODO: Check if server "has tasks" instead of this
        if (Configuration.getInstance().getInterpreter() instanceof RabbitMQPseudoCodeInterpreter) {
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
        openProject.addActionListener(e -> openSelectedProject());
        bottomPanel.add(openProject);

        JButton newProject = new JButton(language.getString("file.new"), new FlatFileChooserNewFolderIcon());
        newProject.addActionListener(e -> new CreateProjectDialog(this).setVisible(true));
        bottomPanel.add(newProject);

        JButton settings = new JButton(language.getString("file.settings"), new FlatFileChooserDetailsViewIcon());
        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(settings);

        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(null);
    }

    private void openSelectedProject() {
        if (!projects.isSelectionEmpty()) {
            logger.info("Opening project '" + projects.getSelectedValue() + "'...");
            new ProjectEditor(projects.getSelectedValue()).setVisible(true);
            dispose();
        }
    }

    /**
     * By calling this method, the list of projects is cleared and rebuilt.
     * A project is only added to the list if it valid. To a project be considered valid, it must be a folder, and
     * have a {@code project.json} file inside it, that contains the following parameters:
     * <ul>
     *     <li>{@code name}: Required. The name of the project, that will be shown in the project list.</li>
     *     <li>{@code version}: Optional. The version of the project.</li>
     * </ul>
     */
    void updateProjectList() {
        projectsModel.clear();

        try {
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
            logger.debug("Project list updated.");
        } catch (IOException e) {
            logger.error("Unable to update project list!");
            logger.error(e.getMessage());
        }
    }

    private static class ProjectListCellRenderer extends DefaultListCellRenderer {
        private List<Language> availableLanguages;

        public ProjectListCellRenderer() {
            availableLanguages = Configuration.getInstance().getInterpreter().getAvailableLanguages();
        }

        @Override
        public Component getListCellRendererComponent(JList<?> jList, Object object, int i, boolean b,
                                                      boolean b1) {
            JLabel component = (JLabel) super.getListCellRendererComponent(jList, object, i, b, b1);

            // This is some horrendous code, I know, but at least it works
            assert object instanceof Project;
            Project project = (Project) object;
            boolean languageAvailable = false;
            for (Language language : availableLanguages) {
                if (language.getName().equals(project.getLanguage().getName()) &&
                        language.getVersion().equals(project.getLanguage().getVersion())) {
                    languageAvailable = true;
                    break;
                }
            }

            String text;
            if (!languageAvailable) {
                if (project.getVersion() != null) {
                    text = String.format("<html>%s - %s (<font color=\"red\">%s</font>)</html>", project.getName(),
                            project.getVersion(), project.getLanguage().toString());
                } else {
                    text = String.format("<html>%s (<font color=\"red\">%s</font>)</html>", project.getName(),
                            project.getLanguage().toString());
                }
            } else {
                if (project.getVersion() != null) {
                    text = String.format("%s - %s (%s)", project.getName(),
                            project.getVersion(), project.getLanguage().toString());
                } else {
                    text = String.format("%s (%s)", project.getName(),
                            project.getLanguage().toString());
                }
            }

            component.setText(text);
            return component;
        }
    }
}
