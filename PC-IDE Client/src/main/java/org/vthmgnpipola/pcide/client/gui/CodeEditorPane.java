package org.vthmgnpipola.pcide.client.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.client.lang.FileSystemWatcher;
import org.vthmgnpipola.pcide.commons.StackTracePrinter;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * A code editor pane is a component that has a {@link RTextScrollPane} with a {@link RSyntaxTextArea} inside, to
 * make it faster and cleaner to create new code editors.
 */
public class CodeEditorPane extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(CodeEditorPane.class);

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
        setSyntax(textArea, path);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        scrollPane.setFoldIndicatorEnabled(true);
        add(scrollPane, BorderLayout.CENTER);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
                    try {
                        Files.write(path, textArea.getText().getBytes());
                        logger.info("Saved file '" + path.toString() + "'.");
                    } catch (IOException ioException) {
                        logger.error("Unable to save file '" + path.toString() + "'!");
                        logger.error(StackTracePrinter.getStackTraceAsString(ioException));
                    }
                }
            }
        });

        FileSystemWatcher.getInstance().registerListener(path, e -> {
            if (e.getEventKind() == ENTRY_MODIFY) {
                try {
                    textArea.setText(Files.readString(path));
                } catch (IOException ioException) {
                    logger.error("Error updating editor of file '" + path.toString() + "'!");
                    logger.error(StackTracePrinter.getStackTraceAsString(ioException));
                }
            }
        });
    }

    private void setSyntax(RSyntaxTextArea textArea, Path path) {
        String filename = path.getFileName().toString();
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        switch (extension) {
            case "properties" -> textArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_PROPERTIES_FILE);
            case "json" -> textArea.setSyntaxEditingStyle("text/pc1");
            case "xml" -> textArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_XML);
            case "yaml", "yml" -> textArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_YAML);
            case "sh" -> textArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_UNIX_SHELL);
            case "bat" -> textArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_WINDOWS_BATCH);
            default -> textArea.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_NONE);
        }
    }
}
