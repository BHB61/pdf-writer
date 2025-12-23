package de.hft_stuttgart.ip1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ScriptEditor extends JFrame {
    private final JTextArea textArea = new JTextArea(30, 120);
    private final ScriptSettings settings = new ScriptSettings();
    private Path currentFile;

    public ScriptEditor() {
        super("PDF Script Editor");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(new JButton(new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent e) {
                openScript();
            }
        }));
        buttons.add(new JButton(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveScript();
            }
        }));
        buttons.add(new JButton(new AbstractAction("Run") {
            @Override
            public void actionPerformed(ActionEvent e) {
                runScript();
            }
        }));
        buttons.add(new JButton(new AbstractAction("Settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSettings();
            }
        }));
        add(buttons, BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void openScript() {
        JFileChooser chooser = new JFileChooser();
        if (currentFile != null) {
            chooser.setSelectedFile(currentFile.toFile());
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile().toPath();
            try {
                textArea.setText(Files.readString(currentFile));
            } catch (IOException ex) {
                showError("Failed to read script: " + ex.getMessage());
            }
        }
    }

    private void saveScript() {
        JFileChooser chooser = new JFileChooser();
        if (currentFile != null) {
            chooser.setSelectedFile(currentFile.toFile());
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile().toPath();
            try {
                Files.writeString(currentFile, textArea.getText());
            } catch (IOException ex) {
                showError("Failed to save script: " + ex.getMessage());
            }
        }
    }

    private void runScript() {
        ScriptRunner runner = new ScriptRunner();
        Path baseDir = Optional.ofNullable(currentFile)
                .map(Path::getParent)
                .orElse(Path.of("."));
        try {
            Path output = runner.run(textArea.getText(), baseDir);
            settings.openPdf(output);
        } catch (IOException ex) {
            showError("Failed to run script: " + ex.getMessage());
        }
    }

    private void editSettings() {
        String current = settings.getViewerCommand();
        String input = JOptionPane.showInputDialog(
                this,
                "Command to open PDFs (use {file} placeholder):",
                current
        );
        if (input != null && !input.isBlank()) {
            settings.setViewerCommand(input.trim());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
