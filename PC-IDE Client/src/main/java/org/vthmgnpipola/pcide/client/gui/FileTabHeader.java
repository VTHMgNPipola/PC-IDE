package org.vthmgnpipola.pcide.client.gui;

import com.formdev.flatlaf.icons.FlatInternalFrameCloseIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * A file tab header is a component to be used in {@link JTabbedPane} tabs. It has a title and a close button.
 *
 * This class could be more generic, but since I'm using this solely for one task, it can be like that (at least for
 * now).
 */
public class FileTabHeader extends JPanel {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Dimension MAXIMUM_CLOSE_BUTTON_SIZE = new Dimension(16, 8);

    private final JTabbedPane tabbedPane;
    private final Path path;

    public FileTabHeader(Path path, String title, JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        this.path = path;
        init(title);
    }

    private void init(String title) {
        setBackground(TRANSPARENT);
        setLayout(new BorderLayout());

        JButton closeButton = new JButton(new FlatInternalFrameCloseIcon());
        closeButton.setPreferredSize(MAXIMUM_CLOSE_BUTTON_SIZE);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(null);
        closeButton.addActionListener(e -> {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                // This will iterate through all tabs until one of them has the same file path as this one. This may
                // remove a tab you don't want to remove since I'm not using a unique identifier, like a UUID, but
                // since I'm going to check if there's a tab open for a path already I'm going to leave it like that.
                if (tabbedPane.getTabComponentAt(i) instanceof FileTabHeader) {
                    FileTabHeader header = (FileTabHeader) tabbedPane.getTabComponentAt(i);
                    if (header.path.equals(this.path)) {
                        tabbedPane.removeTabAt(i);
                        break;
                    }
                }
            }
        });
        add(closeButton, BorderLayout.EAST);

        JLabel tabTitle = new JLabel(title);
        tabTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        add(tabTitle, BorderLayout.CENTER);
    }

    public static void addTab(JComponent content, Path path, String title, JTabbedPane tabbedPane) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            // If there is a tab with the same path open, it will select this tab instead of opening another one.
            if (tabbedPane.getTabComponentAt(i) instanceof FileTabHeader) {
                FileTabHeader header = (FileTabHeader) tabbedPane.getTabComponentAt(i);
                if (header.path.equals(path)) {
                    tabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }

        FileTabHeader header = new FileTabHeader(path, title, tabbedPane);
        tabbedPane.addTab(title, content);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, header);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }
}
