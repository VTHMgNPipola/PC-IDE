package org.vthmgnpipola.pcide.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import org.vthmgnpipola.pcide.client.Configuration;

public class ProjectEditor extends JFrame {
    public ProjectEditor() {
        init();
    }

    private void init() {
        ResourceBundle language = Configuration.getInstance().getLanguage();
        setTitle(language.getString("projectEditor.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        // Menu
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(language.getString("projectEditor.menu.file"));
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu(language.getString("projectEditor.menu.edit"));
        menuBar.add(editMenu);

        // Content
        JPanel contentPane = new JPanel(new BorderLayout());
        Dimension dimension = new Dimension(1024, 576);
        contentPane.setPreferredSize(dimension);

        JPanel leftPanel = new JPanel();
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true)));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JTabbedPane editorTabs = new JTabbedPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, editorTabs);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation((int) (contentPane.getPreferredSize().width * 0.25));
        contentPane.add(splitPane, BorderLayout.CENTER);

        TabHeader.addTab(new CodeEditorPane(), "exemplo.pc", editorTabs);

        setContentPane(contentPane);
        setJMenuBar(menuBar);
        pack();
        setLocationRelativeTo(null);
    }
}
