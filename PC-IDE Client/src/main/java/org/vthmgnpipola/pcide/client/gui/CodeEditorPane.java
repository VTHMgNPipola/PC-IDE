package org.vthmgnpipola.pcide.client.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * A code editor pane is a component that has a {@link RTextScrollPane} with a {@link RSyntaxTextArea} inside, to
 * make it faster and cleaner to create new code editors.
 */
public class CodeEditorPane extends JPanel {
    public CodeEditorPane(Path path) throws IOException {
        init(path);
    }

    private void init(Path path) throws IOException {
        setLayout(new BorderLayout());

        RSyntaxTextArea textArea = new RSyntaxTextArea(Files.readString(path));
        textArea.setAntiAliasingEnabled(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setAnimateBracketMatching(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setCloseCurlyBraces(true);
        textArea.setCloseMarkupTags(true);
        textArea.setCodeFoldingEnabled(true);
        textArea.setMarkOccurrences(true);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        scrollPane.setFoldIndicatorEnabled(true);
        add(scrollPane, BorderLayout.CENTER);
    }
}
