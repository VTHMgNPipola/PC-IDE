package org.vthmgnpipola.pcide.client.gui;

import com.formdev.flatlaf.icons.FlatInternalFrameCloseIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class TabHeader extends JPanel {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Dimension MAXIMUM_CLOSE_BUTTON_SIZE = new Dimension(16, 8);

    private final JTabbedPane tabbedPane;
    private final int index;

    public TabHeader(String title, JTabbedPane tabbedPane, int index) {
        this.tabbedPane = tabbedPane;
        this.index = index;
        init(title);
    }

    private void init(String title) {
        setBackground(TRANSPARENT);
        setLayout(new BorderLayout());

        JButton closeButton = new JButton(new FlatInternalFrameCloseIcon());
        closeButton.setPreferredSize(MAXIMUM_CLOSE_BUTTON_SIZE);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(null);
        closeButton.addActionListener(e -> tabbedPane.removeTabAt(index));
        add(closeButton, BorderLayout.EAST);

        JLabel tabTitle = new JLabel(title);
        tabTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        add(tabTitle, BorderLayout.CENTER);
    }

    public static void addTab(JComponent content, String title, JTabbedPane tabbedPane) {
        TabHeader header = new TabHeader(title, tabbedPane, tabbedPane.getTabCount());
        tabbedPane.addTab(title, content);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, header);
    }
}
