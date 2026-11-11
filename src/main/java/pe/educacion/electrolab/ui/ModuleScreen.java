package pe.educacion.electrolab.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;

public abstract class ModuleScreen extends JPanel {
    protected final JPanel settingsGrid = Ui.fieldGrid();
    protected final Ui.DiagramPanel diagram = new Ui.DiagramPanel();
    protected final JLabel heroLabel = Ui.label("—", 30, true, Theme.TEAL);
    protected final JLabel heroTitle = Ui.label("Resultado", 11, true, Theme.MUTED);
    protected final JLabel heroDetail = Ui.label("Ajusta los valores para explorar.", 12, false, Theme.MUTED);
    protected final JLabel formulaLabel = Ui.label("", 12, true, Theme.BLUE);
    protected final JPanel metricsGrid = new JPanel(new GridLayout(2, 2, 9, 9));
    protected JPanel inputCard;
    protected final JTextArea explanation = new JTextArea();
    private final String moduleId;
    private final List<Ui.Input> trackedInputs = new ArrayList<>();
    private final List<Ui.Choice> trackedChoices = new ArrayList<>();
    private final List<String[]> outputRows = new ArrayList<>();
    private final Ui.PlotPanel plot = new Ui.PlotPanel();
    private final JTextArea developmentArea = new JTextArea();
    private JToggleButton developmentToggle;

    protected ModuleScreen(String number, String category, String title, String subtitle, String conceptTitle, String conceptText) {
        moduleId = number;
        setOpaque(true);
        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(0, 18));
        setBorder(BorderFactory.createEmptyBorder(27, 30, 28, 30));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.add(Ui.eyebrow("SIMULADOR " + number + "   /   " + category));
        Ui.addGap(header, 6);
        header.add(Ui.label(title, 27, true, Theme.TEXT));
        Ui.addGap(header, 4);
        header.add(Ui.label(subtitle, 12, false, Theme.MUTED));
        add(header, BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 17, 0));
        columns.setOpaque(false);
        columns.add(makeLeftColumn());
        columns.add(makeRightColumn(conceptTitle, conceptText));
        add(columns, BorderLayout.CENTER);
    }

    private JPanel makeLeftColumn() {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new javax.swing.BoxLayout(column, javax.swing.BoxLayout.Y_AXIS));

        inputCard = Ui.card();
        inputCard.setLayout(new BorderLayout(0, 13));
        inputCard.setBorder(BorderFactory.createEmptyBorder(17, 17, 16, 17));
        JPanel inputHeading = new JPanel();
        inputHeading.setOpaque(false);
        inputHeading.setLayout(new javax.swing.BoxLayout(inputHeading, javax.swing.BoxLayout.Y_AXIS));
        inputHeading.add(Ui.label("Configura la simulación", 15, true, Theme.TEXT));
        Ui.addGap(inputHeading, 3);
        inputHeading.add(Ui.label("Escribe tus valores; el resultado se actualiza al instante.", 11, false, Theme.MUTED));
        inputCard.add(inputHeading, BorderLayout.NORTH);
        inputCard.add(settingsGrid, BorderLayout.CENTER);
        inputCard.add(makeActionButtons(), BorderLayout.SOUTH);

        Ui.CardPanel result = Ui.card();
        result.setLayout(new BorderLayout(0, 13));
        result.setBorder(BorderFactory.createEmptyBorder(17, 17, 16, 17));
        JPanel hero = new JPanel(new BorderLayout(10, 0));
        hero.setOpaque(false);
        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new javax.swing.BoxLayout(heroText, javax.swing.BoxLayout.Y_AXIS));
        heroText.add(heroTitle);
        Ui.addGap(heroText, 3);
        heroText.add(heroLabel);
        hero.add(heroText, BorderLayout.CENTER);
        hero.add(Ui.label("EN TIEMPO REAL", 9, true, Theme.SUBTLE), BorderLayout.EAST);
        JPanel resultTop = new JPanel(new BorderLayout(0, 8));
        resultTop.setOpaque(false);
        resultTop.add(hero, BorderLayout.CENTER);
        resultTop.add(heroDetail, BorderLayout.SOUTH);
        result.add(resultTop, BorderLayout.NORTH);

        metricsGrid.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            metricsGrid.add(Ui.metric("—", "—", ""));
        }
        result.add(metricsGrid, BorderLayout.CENTER);
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new javax.swing.BoxLayout(footer, javax.swing.BoxLayout.Y_AXIS));
        JPanel formula = new JPanel();
        formula.setOpaque(false);
        formula.setLayout(new javax.swing.BoxLayout(formula, javax.swing.BoxLayout.Y_AXIS));
        formula.add(Ui.eyebrow("Modelo físico"));
        Ui.addGap(formula, 4);
        formula.add(formulaLabel);
        footer.add(formula);
        Ui.addGap(footer, 7);
        developmentToggle = new JToggleButton("Ver desarrollo paso a paso  +");
        styleActionButton(developmentToggle);
        developmentToggle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        developmentToggle.addActionListener(event -> {
            developmentArea.setVisible(developmentToggle.isSelected());
            developmentToggle.setText(developmentToggle.isSelected() ? "Ocultar desarrollo  −" : "Ver desarrollo paso a paso  +");
            revalidate();
        });
        footer.add(developmentToggle);
        developmentArea.setEditable(false);
        developmentArea.setLineWrap(true);
        developmentArea.setWrapStyleWord(true);
        developmentArea.setOpaque(false);
        developmentArea.setForeground(Theme.MUTED);
        developmentArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        developmentArea.setRows(4);
        developmentArea.setBorder(BorderFactory.createEmptyBorder(7, 4, 0, 4));
        developmentArea.setVisible(false);
        footer.add(developmentArea);
        result.add(footer, BorderLayout.SOUTH);

        column.add(inputCard);
        Ui.addGap(column, 13);
        column.add(result);
        column.add(javax.swing.Box.createVerticalGlue());
        return column;
    }

    private JPanel makeRightColumn(String conceptTitle, String conceptText) {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new javax.swing.BoxLayout(column, javax.swing.BoxLayout.Y_AXIS));

        Ui.CardPanel visual = Ui.card();
        visual.setLayout(new BorderLayout(0, 10));
        visual.setBorder(BorderFactory.createEmptyBorder(15, 16, 12, 16));
        JPanel visualHeader = new JPanel(new BorderLayout());
        visualHeader.setOpaque(false);
        visualHeader.add(Ui.label("Vista del fenómeno", 14, true, Theme.TEXT), BorderLayout.WEST);
        visualHeader.add(Ui.label("ESQUEMA", 9, true, Theme.SUBTLE), BorderLayout.EAST);
        visual.add(visualHeader, BorderLayout.NORTH);
        JTabbedPane visualTabs = new JTabbedPane();
        visualTabs.addTab("Esquema", diagram);
        visualTabs.addTab("Gráfica", plot);
        visualTabs.setOpaque(false);
        visualTabs.setForeground(Theme.MUTED);
        visual.add(visualTabs, BorderLayout.CENTER);

        Ui.CardPanel concept = Ui.card();
        concept.setLayout(new BorderLayout(0, 8));
        concept.setBorder(BorderFactory.createEmptyBorder(15, 16, 16, 16));
        concept.add(Ui.label(conceptTitle, 14, true, Theme.TEXT), BorderLayout.NORTH);
        explanation.setText(conceptText);
        explanation.setWrapStyleWord(true);
        explanation.setLineWrap(true);
        explanation.setEditable(false);
        explanation.setFocusable(false);
        explanation.setOpaque(false);
        explanation.setForeground(Theme.MUTED);
        explanation.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        explanation.setBorder(BorderFactory.createEmptyBorder());
        explanation.setRows(4);
        JScrollPane noteScroll = new JScrollPane(explanation);
        noteScroll.setBorder(BorderFactory.createEmptyBorder());
        noteScroll.setOpaque(false);
        noteScroll.getViewport().setOpaque(false);
        concept.add(noteScroll, BorderLayout.CENTER);

        column.add(visual);
        Ui.addGap(column, 13);
        column.add(concept);
        column.add(javax.swing.Box.createVerticalGlue());
        return column;
    }

    protected void setResult(String title, String value, String detail, String formula,
                             String[] metric1, String[] metric2, String[] metric3, String[] metric4) {
        heroTitle.setText(title);
        heroLabel.setText(value);
        heroDetail.setText(detail);
        formulaLabel.setText(formula);
        outputRows.clear();
        outputRows.add(new String[]{"Resultado", title, value, detail});
        outputRows.add(new String[]{"Fórmula", formula, "", ""});
        String[][] metrics = {metric1, metric2, metric3, metric4};
        for (int i = 0; i < metrics.length; i++) {
            JPanel card = (JPanel) metricsGrid.getComponent(i);
            JLabel name = (JLabel) card.getComponent(0);
            name.setText(metrics[i][0].toUpperCase(java.util.Locale.ROOT));
            Ui.setMetric(card, metrics[i][1], metrics[i][2]);
            outputRows.add(new String[]{"Magnitud", metrics[i][0], metrics[i][1], metrics[i][2]});
        }
        revalidate();
        repaint();
    }

    protected void setInvalid(String message) {
        heroLabel.setText("—");
        heroDetail.setText(message);
        heroDetail.setForeground(Theme.YELLOW);
        outputRows.clear();
        outputRows.add(new String[]{"Aviso", "Entrada inválida", message, ""});
        plot.clear();
    }

    protected void setValidDetailColor() {
        heroDetail.setForeground(Theme.MUTED);
    }

    protected static String[] metric(String name, double value, String unit) {
        return new String[]{name, Ui.number(value), unit};
    }

    protected static String[] metric(String name, String value, String unit) {
        return new String[]{name, value, unit};
    }

    protected static void inputListener(Ui.Input input, Runnable action) {
        Ui.onChange(input.field, action);
    }

    protected static void comboListener(Ui.Choice choice, Runnable action) {
        choice.combo.addActionListener(e -> action.run());
    }

    protected final void addSetting(Ui.Input input) {
        trackedInputs.add(input);
        settingsGrid.add(input.panel);
    }

    protected final void registerSetting(Ui.Input input) {
        trackedInputs.add(input);
    }

    protected final void addSetting(Ui.Choice choice) {
        trackedChoices.add(choice);
        settingsGrid.add(choice.panel);
    }

    protected final void registerSetting(Ui.Choice choice) {
        trackedChoices.add(choice);
    }

    protected void saveCustomConfiguration(Properties properties) {
    }

    protected void loadCustomConfiguration(Properties properties) {
    }

    protected final void setSteps(String... steps) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < steps.length; i++) {
            text.append(i + 1).append(". ").append(steps[i]);
            if (i + 1 < steps.length) {
                text.append("\n\n");
            }
        }
        developmentArea.setText(text.toString());
    }

    protected final void setGraph(String xLabel, String yLabel, double[] x, double[] y, double markerX, double markerY) {
        plot.setData(xLabel, yLabel, x, y, markerX, markerY);
    }

    protected void openChallenge() {
        String[] answers = challengeAnswers();
        int selected = JOptionPane.showOptionDialog(this, challengeQuestion(), "Reto de práctica",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, answers, answers[0]);
        if (selected < 0) {
            return;
        }
        boolean correct = selected == correctAnswerIndex();
        JOptionPane.showMessageDialog(this,
                (correct ? "¡Correcto! " : "Repasa la idea. ") + challengeExplanation(),
                correct ? "Respuesta correcta" : "Pista", correct ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.PLAIN_MESSAGE);
    }

    protected String challengeQuestion() {
        return "¿Qué conviene revisar antes de aceptar un resultado?";
    }

    protected String[] challengeAnswers() {
        return new String[]{"Unidades y sentido físico", "Solo el número", "Nada si parece razonable"};
    }

    protected int correctAnswerIndex() {
        return 0;
    }

    protected String challengeExplanation() {
        return "Comprueba unidades, signo y tendencia con la relación física usada.";
    }

    private JPanel makeActionButtons() {
        JPanel actions = new JPanel(new GridLayout(1, 5, 6, 0));
        actions.setOpaque(false);
        JButton save = actionButton("Guardar");
        JButton load = actionButton("Cargar");
        JButton export = actionButton("Exportar CSV");
        JButton image = actionButton("Informe PNG");
        JButton challenge = actionButton("Reto");
        save.addActionListener(event -> saveConfiguration());
        load.addActionListener(event -> loadConfiguration());
        export.addActionListener(event -> exportCsv());
        image.addActionListener(event -> exportImage());
        challenge.addActionListener(event -> openChallenge());
        actions.add(save);
        actions.add(load);
        actions.add(export);
        actions.add(image);
        actions.add(challenge);
        return actions;
    }

    private static JButton actionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setForeground(Theme.TEAL);
        button.setBackground(Theme.CARD_ALT);
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE, 1, true), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return button;
    }

    private static void styleActionButton(JToggleButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setForeground(Theme.TEAL);
        button.setBackground(Theme.CARD_ALT);
        button.setFocusPainted(false);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE, 1, true), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    private void saveConfiguration() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar configuración del simulador");
        chooser.setSelectedFile(new java.io.File("electrolab-modulo-" + moduleId + ".properties"));
        chooser.setFileFilter(new FileNameExtensionFilter("Configuración ElectroLab (*.properties)", "properties"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = ensureExtension(chooser.getSelectedFile(), ".properties");
        Properties properties = new Properties();
        properties.setProperty("module", moduleId);
        for (int i = 0; i < trackedInputs.size(); i++) {
            properties.setProperty("input." + i, trackedInputs.get(i).field.getText());
            properties.setProperty("inputName." + i, trackedInputs.get(i).title);
        }
        for (int i = 0; i < trackedChoices.size(); i++) {
            properties.setProperty("choice." + i, String.valueOf(trackedChoices.get(i).combo.getSelectedItem()));
        }
        saveCustomConfiguration(properties);
        try (var output = Files.newOutputStream(file.toPath())) {
            properties.store(output, "Configuración de ElectroLab");
            JOptionPane.showMessageDialog(this, "Configuración guardada.", "ElectroLab", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            showFileError(exception);
        }
    }

    private void loadConfiguration() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Cargar configuración del simulador");
        chooser.setFileFilter(new FileNameExtensionFilter("Configuración ElectroLab (*.properties)", "properties"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Properties properties = new Properties();
        try (var input = Files.newInputStream(chooser.getSelectedFile().toPath())) {
            properties.load(input);
            if (!moduleId.equals(properties.getProperty("module"))) {
                JOptionPane.showMessageDialog(this, "Ese archivo pertenece a otro simulador.", "Configuración incompatible", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (int i = 0; i < trackedInputs.size(); i++) {
                String value = properties.getProperty("input." + i);
                if (value != null) {
                    trackedInputs.get(i).field.setText(value);
                }
            }
            for (int i = 0; i < trackedChoices.size(); i++) {
                String value = properties.getProperty("choice." + i);
                if (value != null) {
                    trackedChoices.get(i).combo.setSelectedItem(value);
                }
            }
            loadCustomConfiguration(properties);
            JOptionPane.showMessageDialog(this, "Configuración cargada.", "ElectroLab", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            showFileError(exception);
        }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar datos del experimento");
        chooser.setSelectedFile(new java.io.File("electrolab-modulo-" + moduleId + ".csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("Datos CSV (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = ensureExtension(chooser.getSelectedFile(), ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("Tipo,Nombre,Valor,Unidad o detalle\n");
            for (Ui.Input input : trackedInputs) {
                writeCsvRow(writer, "Entrada", input.title, input.field.getText(), "");
            }
            for (Ui.Choice choice : trackedChoices) {
                writeCsvRow(writer, "Opción", choice.title, String.valueOf(choice.combo.getSelectedItem()), "");
            }
            for (String[] row : outputRows) {
                writeCsvRow(writer, row);
            }
            for (String[] row : plot.dataRows()) {
                writeCsvRow(writer, row);
            }
            JOptionPane.showMessageDialog(this, "Resultados exportados a CSV.", "ElectroLab", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            showFileError(exception);
        }
    }

    private void exportImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar resumen visual del experimento");
        chooser.setSelectedFile(new java.io.File("electrolab-modulo-" + moduleId + ".png"));
        chooser.setFileFilter(new FileNameExtensionFilter("Imagen PNG (*.png)", "png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File file = ensureExtension(chooser.getSelectedFile(), ".png");
        int imageWidth = 1040;
        int imageHeight = 390;
        int panelWidth = 490;
        int panelHeight = 270;
        java.awt.Dimension oldDiagramSize = diagram.getSize();
        java.awt.Dimension oldPlotSize = plot.getSize();
        try {
            diagram.setSize(panelWidth, panelHeight);
            plot.setSize(panelWidth, panelHeight);
            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Theme.BACKGROUND);
            graphics.fillRect(0, 0, imageWidth, imageHeight);
            graphics.setColor(Theme.TEAL);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 13));
            graphics.drawString("ElectroLab · Módulo " + moduleId, 24, 28);
            graphics.setColor(Theme.TEXT);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 17));
            graphics.drawString(outputRows.isEmpty() ? "Resultado de la simulación" : outputRows.get(0)[1] + ": " + outputRows.get(0)[2], 24, 58);
            graphics.setColor(Theme.MUTED);
            graphics.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            graphics.drawString(outputRows.size() > 1 ? outputRows.get(1)[1] : "", 24, 78);
            graphics.setColor(Theme.TEXT);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 11));
            graphics.drawString("Esquema", 24, 101);
            graphics.drawString("Gráfica de la relación", 534, 101);
            Graphics2D diagramGraphics = (Graphics2D) graphics.create(24, 110, panelWidth, panelHeight);
            diagram.paint(diagramGraphics);
            diagramGraphics.dispose();
            Graphics2D plotGraphics = (Graphics2D) graphics.create(534, 110, panelWidth, panelHeight);
            plot.paint(plotGraphics);
            plotGraphics.dispose();
            graphics.dispose();
            ImageIO.write(image, "png", file);
            JOptionPane.showMessageDialog(this, "Resumen visual guardado como PNG.", "ElectroLab", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            showFileError(exception);
        } finally {
            diagram.setSize(oldDiagramSize);
            plot.setSize(oldPlotSize);
        }
    }

    private static void writeCsvRow(BufferedWriter writer, String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                writer.write(',');
            }
            writer.write('"');
            writer.write(values[i].replace("\"", "\"\""));
            writer.write('"');
        }
        writer.write('\n');
    }

    private static java.io.File ensureExtension(java.io.File file, String extension) {
        if (file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(extension)) {
            return file;
        }
        return new java.io.File(file.getParentFile(), file.getName() + extension);
    }

    private void showFileError(IOException exception) {
        JOptionPane.showMessageDialog(this, "No se pudo completar la operación de archivo.\n" + exception.getMessage(),
                "Error de archivo", JOptionPane.ERROR_MESSAGE);
    }
}
