package org.vthmgnpipola.pcide.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.client.Configuration;
import org.vthmgnpipola.pcide.client.lang.Project;

/**
 * The project editor is where the code is actually made and executed. The editor is composed of three main parts:
 * the project file tree, the editor tabs, and the toolbar.
 *
 * The project file tree shows all the files in the project. The editor tabs is where you can actually edit code.
 * The toolbar has useful buttons to perform actions in the code (such as the run button).
 */
public class ProjectEditor extends JFrame {
    private Logger logger = LoggerFactory.getLogger(ProjectEditor.class);

    private Project project;

    private FileMutableTreeNode projectParentDirectoryNode;

    private JTabbedPane editorTabs;

    public ProjectEditor(Project project) {
        this.project = project;
        init(project);
    }

    private void init(Project project) {
        ResourceBundle language = Configuration.getInstance().getLanguage();
        setTitle(language.getString("projectEditor.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                new ProjectDashboard().setVisible(true);
            }
        });

        // Content
        JPanel contentPane = new JPanel(new BorderLayout());
        Dimension dimension = new Dimension(1024, 576);
        contentPane.setPreferredSize(dimension);

        projectParentDirectoryNode = new FileMutableTreeNode(new FileNode(project.path()));
        updateFileTree();
        JTree fileTree = new JTree(projectParentDirectoryNode);
        // TODO: If the file is already open switch to its tab instead of creating another one.
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    DefaultMutableTreeNode selectedNode =
                            (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                    if (selectedNode != null && selectedNode.getUserObject() instanceof FileNode fileNode &&
                            Files.isRegularFile(fileNode.path)) {
                        Path path = fileNode.path;
                        try {
                            TabHeader.addTab(new CodeEditorPane(path), path.getFileName().toString(), editorTabs);
                        } catch (IOException ioException) {
                            logger.error("Error opening editor on file '" + path.toString() + "'!");
                            logger.error(ioException.getMessage());
                        }
                    }
                }
            }
        });

        editorTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(fileTree), editorTabs);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation((int) (contentPane.getPreferredSize().width * 0.25));
        contentPane.add(splitPane, BorderLayout.CENTER);

        // Menu
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(language.getString("projectEditor.menu.file"));
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu(language.getString("projectEditor.menu.edit"));
        menuBar.add(editMenu);

        setContentPane(contentPane);
        setJMenuBar(menuBar);
        pack();
        setLocationRelativeTo(null);
    }

    private void updateFileTree() {
        projectParentDirectoryNode.removeAllChildren();
        walkDirectory(projectParentDirectoryNode);
    }

    private void walkDirectory(FileMutableTreeNode node) {
        if (node.getUserObject() instanceof FileNode fileNode) {
            try {
                Files.list(fileNode.path).forEachOrdered(p -> {
                    FileMutableTreeNode newNode = new FileMutableTreeNode(new FileNode(p));
                    node.add(newNode);
                    if (Files.isDirectory(p)) {
                        walkDirectory(newNode);
                    }
                });
            } catch (IOException e) {
                logger.error("Unable to walk on directory at '" + fileNode.path.toString() + "'!");
                logger.error(e.getMessage());
                System.exit(-1);
            }
        }
    }

    /**
     * This type of tree node is exactly like a {@link DefaultMutableTreeNode}, with the exception that it only
     * return true to {@link #isLeaf()} if the file it is associated with is a regular file.
     */
    private static class FileMutableTreeNode extends DefaultMutableTreeNode {
        public FileMutableTreeNode(FileNode fileNode) {
            super(fileNode);
        }

        @Override
        public boolean isLeaf() {
            return Files.isRegularFile(((FileNode) userObject).path);
        }
    }

    /**
     * A FileNode will store a {@link Path}, and its {@link #toString()} method will return the name of the
     * file/directory this Path points to.
     */
    private static record FileNode(Path path) {
        @Override
        public String toString() {
            return path.getFileName().toString();
        }
    }
}
