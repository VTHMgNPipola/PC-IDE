package org.vthmgnpipola.pcide.client.gui;

import com.formdev.flatlaf.icons.FlatFileChooserDetailsViewIcon;
import com.formdev.flatlaf.icons.FlatFileChooserNewFolderIcon;
import com.formdev.flatlaf.icons.FlatFileViewDirectoryIcon;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
import org.vthmgnpipola.pcide.client.Configuration;
import org.vthmgnpipola.pcide.client.lang.ServerPseudoCodeInterpreter;

/**
 * The project dashboard is where projects (or tasks, if the client is connected to a server) are shown, and can be
 * selected or removed.
 */
public class ProjectDashboard extends JFrame {
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
        DefaultListModel<String> projectsModel = new DefaultListModel<>();
        projectsModel.addElement("Projeto 1");
        projectsModel.addElement("Projeto 2");
        JList<String> projects = new JList<>(projectsModel);
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
            ProjectEditor editor = new ProjectEditor();
            editor.setVisible(true);
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
}
