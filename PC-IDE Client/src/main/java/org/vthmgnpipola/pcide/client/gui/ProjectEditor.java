package org.vthmgnpipola.pcide.client.gui;

import com.formdev.flatlaf.icons.FlatFileViewFileIcon;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.client.Configuration;
import org.vthmgnpipola.pcide.client.lang.FileSystemWatcher;
import org.vthmgnpipola.pcide.client.lang.Project;
import org.vthmgnpipola.pcide.commons.StackTracePrinter;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

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

    private JTree fileTree;

    public ProjectEditor(Project project) {
        this.project = project;
        init(project);
    }

    private void init(Project project) {
        ResourceBundle language = Configuration.getInstance().getLanguage();
        setTitle(language.getString("projectEditor.title") + " - " + project.getName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                FileSystemWatcher.getInstance().unregisterListener(project.getPath());
                new ProjectDashboard().setVisible(true);
            }
        });

        // Menu
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(language.getString("projectEditor.menu.file"));
        JMenuItem newMenuItem = new JMenuItem(language.getString("projectEditor.menu.file.new"),
                new FlatFileViewFileIcon());
        newMenuItem.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(this,
                    language.getString("projectEditor.menu.file.new.description"),
                    language.getString("projectEditor.menu.file.new"), JOptionPane.QUESTION_MESSAGE);
            createFile(project.getPath(), fileName);
        });
        fileMenu.add(newMenuItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu(language.getString("projectEditor.menu.edit"));
        menuBar.add(editMenu);
        setJMenuBar(menuBar);

        // Content
        JPanel contentPane = new JPanel(new BorderLayout());
        Dimension dimension = new Dimension(1024, 576);
        contentPane.setPreferredSize(dimension);

        projectParentDirectoryNode = new FileMutableTreeNode(new FileNode(project.getPath()));
        updateFileTree();

        fileTree = new JTree(projectParentDirectoryNode);
        JPopupMenu fileTreeMenu = new JPopupMenu();
        JMenuItem newFileTreeFile = new JMenuItem(language.getString("projectEditor.menu.file.new"),
                new FlatFileViewFileIcon());
        newFileTreeFile.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(this,
                    language.getString("projectEditor.menu.file.new.description"),
                    language.getString("projectEditor.menu.file.new"), JOptionPane.QUESTION_MESSAGE);
            Path path = project.getPath();
            TreePath selection = fileTree.getSelectionPath();
            if (selection != null) {
                FileMutableTreeNode[] nodes = Arrays.copyOf(selection.getPath(), selection.getPath().length,
                        FileMutableTreeNode[].class);
                if (Files.isDirectory(((FileNode) nodes[nodes.length - 1].getUserObject()).path)) {
                    path = ((FileNode) nodes[nodes.length - 1].getUserObject()).path;
                } else { // If the selected path is a file the parent must be a directory
                    path = ((FileNode) nodes[nodes.length - 2].getUserObject()).path;
                }
            }
            createFile(path, fileName);
            updateFileTree();
        });
        JMenuItem updateFileTree = new JMenuItem(language.getString("projectEditor.menu.file.update"));
        updateFileTree.addActionListener(e -> updateFileTree());
        fileTreeMenu.add(newFileTreeFile);
        fileTreeMenu.add(updateFileTree);
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1) {
                    DefaultMutableTreeNode selectedNode =
                            (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                    if (selectedNode != null && selectedNode.getUserObject() instanceof FileNode) {
                        FileNode fileNode = (FileNode) selectedNode.getUserObject();
                        Path path = fileNode.path;
                        if (Files.isRegularFile(path)) {
                            try {
                                FileTabHeader.addTab(new CodeEditorPane(path), path,
                                        path.getFileName().toString(), editorTabs);
                            } catch (IOException ioException) {
                                logger.error("Error opening editor on file '" + path.toString() + "'!");
                                logger.error(StackTracePrinter.getStackTraceAsString(ioException));
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    fileTreeMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        fileTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F5) {
                    updateFileTree();
                }
            }
        });

        editorTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(fileTree), editorTabs);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation((int) (contentPane.getPreferredSize().width * 0.25));
        contentPane.add(splitPane, BorderLayout.CENTER);

        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(null);

        FileSystemWatcher.getInstance().setWorkingDirectory(project.getPath());
        FileSystemWatcher.getInstance().registerListener(project.getPath(), e -> {
            if (e.getEventKind() == ENTRY_CREATE || e.getEventKind() == ENTRY_DELETE) {
                updateFileTree();
            }
        });
    }

    private void createFile(Path basePath, String fileName) {
        if (fileName != null) {
            Path filePath = basePath.resolve(fileName);
            try {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
                updateFileTree();
            } catch (IOException ioException) {
                logger.error("Error creating file/directory!");
                logger.error(StackTracePrinter.getStackTraceAsString(ioException));
            }
        }
    }

    private void updateFileTree() {
        projectParentDirectoryNode.removeAllChildren();
        walkDirectory(projectParentDirectoryNode);
        if (fileTree != null) {
            fileTree.updateUI();
        }
    }

    private void walkDirectory(FileMutableTreeNode node) {
        if (node.getUserObject() instanceof FileNode) {
            FileNode fileNode = (FileNode) node.getUserObject();
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
                logger.error(StackTracePrinter.getStackTraceAsString(e));
                System.exit(-1);
            }
        }
    }

    /**
     * This type of tree node is exactly like a {@link DefaultMutableTreeNode}, with the exception that it only
     * return true to {@link #isLeaf()} if the file it is associated with is a regular file.
     */
    private static class FileMutableTreeNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 7856442366118343457L;

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
    private static class FileNode {
        private final Path path;

        public FileNode(Path path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return path.getFileName().toString();
        }
    }
}
