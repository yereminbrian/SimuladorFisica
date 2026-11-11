package pe.educacion.electrolab.modules;

import pe.educacion.electrolab.ui.ModuleScreen;
import pe.educacion.electrolab.ui.Theme;
import pe.educacion.electrolab.ui.Ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Locale;
import java.util.Properties;

public final class ChargesScreen extends ModuleScreen {
    private static final double K = 8.9875517923e9;
    private final Ui.Choice experiment = Ui.choice("Experimento", "modo", new String[]{
            "Fuerza entre cargas (1D)", "Campo eléctrico en plano (2D)"
    }, "Cambia entre la ley de Coulomb en una línea y el mapa vectorial en dos dimensiones.");
    private final Ui.Input q1 = Ui.input("Carga q₁", "nC", "3", "Carga puntual fuente; puede ser positiva o negativa.");
    private final Ui.Input q2 = Ui.input("Carga q₂", "nC", "-2", "En 1D es la carga de prueba; en 2D es otra fuente del campo.");
    private final Ui.Input distance = Ui.input("Separación r", "cm", "5", "Distancia entre q₁ y q₂ para el experimento en línea.");
    private final Ui.Input q3 = Ui.input("Tercera carga q₃", "nC", "0", "Fuente adicional; usa 0 para desactivarla.");
    private final Ui.Input distance2 = Ui.input("Separación q₂–q₃", "cm", "5", "Distancia desde q₂ hasta q₃, sobre la misma línea.");
    private final Ui.Input q4 = Ui.input("Cuarta carga q₄", "nC", "1.5", "Cuarta fuente del campo 2D; usa 0 para desactivarla.");
    private final double[] chargeX = {-2.0, 2.0, 0.0, 0.0};
    private final double[] chargeY = {0.0, 0.0, 2.0, -2.0};
    private double testX;
    private double testY;
    private int draggedChargeIndex = -1;
    private JPanel modeCards;

    public ChargesScreen() {
        super("01", "ELECTROSTÁTICA", "Cargas y campo eléctrico",
                "Explora la ley de Coulomb en una línea o el campo eléctrico completo en 2D.",
                "Idea clave",
                "La ley de Coulomb describe la fuerza entre cargas puntuales. El campo eléctrico neto es la suma vectorial de los campos de cada fuente. En el modo 2D puedes arrastrar cada carga, seleccionar un punto P con un clic y observar las líneas de campo y las equipotenciales.");
        buildSettings();
        distance.addSlider(2, 30, 5, "cm");
        distance2.addSlider(2, 30, 5, "cm");
        for (Ui.Input input : new Ui.Input[]{q1, q2, distance, q3, distance2, q4}) {
            inputListener(input, this::calculate);
        }
        comboListener(experiment, this::showSelectedMode);
        diagram.setPressHandler(this::startInteraction);
        diagram.setDragHandler(this::dragInteraction);
        diagram.setClickHandler(this::clickOnDiagram);
        diagram.setDragEndHandler(() -> draggedChargeIndex = -1);
        showSelectedMode();
        calculate();
    }

    private void buildSettings() {
        settingsGrid.setLayout(new java.awt.BorderLayout());
        registerSetting(experiment);
        for (Ui.Input input : new Ui.Input[]{q1, q2, distance, q3, distance2, q4}) registerSetting(input);

        modeCards = new JPanel(new java.awt.BorderLayout());
        modeCards.setOpaque(false);
        JPanel configuration = Ui.verticalPanel(8);
        configuration.add(experiment.panel);
        configuration.add(modeCards);
        settingsGrid.add(configuration, java.awt.BorderLayout.NORTH);
    }

    private void showSelectedMode() {
        boolean plane = experiment.combo.getSelectedIndex() == 1;
        modeCards.removeAll();
        if (plane) {
            JPanel planeSettings = new JPanel(new java.awt.BorderLayout(0, 8));
            planeSettings.setOpaque(false);
            JPanel chargeGrid = new JPanel(new GridLayout(2, 2, 10, 10));
            chargeGrid.setOpaque(false);
            chargeGrid.add(q1.panel);
            chargeGrid.add(q2.panel);
            chargeGrid.add(q3.panel);
            chargeGrid.add(q4.panel);
            JLabel instructions = Ui.label("Punto de prueba: clic en el plano. Mueve q₁–q₄ arrastrándolas. El eje va de −5 a 5 m.", 10, false, Theme.MUTED);
            instructions.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 3, 0, 3));
            planeSettings.add(chargeGrid, java.awt.BorderLayout.CENTER);
            planeSettings.add(instructions, java.awt.BorderLayout.SOUTH);
            modeCards.add(planeSettings, java.awt.BorderLayout.CENTER);
        } else {
            JPanel lineSettings = Ui.fieldGrid();
            lineSettings.add(q1.panel);
            lineSettings.add(q2.panel);
            lineSettings.add(distance.panel);
            lineSettings.add(q3.panel);
            lineSettings.add(distance2.panel);
            modeCards.add(lineSettings, java.awt.BorderLayout.CENTER);
        }
        draggedChargeIndex = -1;
        calculate();
        settingsGrid.revalidate();
        settingsGrid.repaint();
    }

    private void calculate() {
        try {
            double source = Ui.read(q1);
            double test = Ui.read(q2);
            double third = Ui.read(q3);
            double fourth = Ui.read(q4);
            if (!Double.isFinite(source) || !Double.isFinite(test) || !Double.isFinite(third) || !Double.isFinite(fourth)) {
                setInvalid("Usa cargas numéricas finitas.");
                return;
            }
            if (experiment.combo.getSelectedIndex() == 1) {
                calculatePlane(new double[]{source, test, third, fourth});
            } else {
                calculateLine(source, test, third);
            }
        } catch (NumberFormatException ex) {
            setInvalid("Completa las cargas y separaciones con números válidos.");
        }
    }

    private void calculateLine(double source, double test, double third) {
        try {
            double q1c = source * 1e-9;
            double q2c = test * 1e-9;
            double r = Ui.read(distance) / 100.0;
            double q3c = third * 1e-9;
            double r3 = Ui.read(distance2) / 100.0;
            if (!Double.isFinite(r) || !Double.isFinite(r3) || r <= 0 || r3 <= 0) {
                setInvalid("Usa separaciones mayores que cero.");
                return;
            }
            double field1 = K * q1c / (r * r);
            double field3 = -K * q3c / (r3 * r3);
            double field = field1 + field3;
            double signedForce = q2c * field;
            double force = Math.abs(signedForce);
            double potential = K * q1c / r + K * q3c / r3;
            double energy = q2c * potential;
            String direction = signedForce > 0 ? "hacia la derecha" : signedForce < 0 ? "hacia la izquierda" : "fuerza neta cero";
            setValidDetailColor();
            setResult("Fuerza neta sobre q₂", Ui.number(force) + " N", "Dirección de la fuerza: " + direction + ". q₃ = 0 desactiva la segunda fuente.",
                    "Etotal = Σ k qᵢ / rᵢ²   ·   F₂ = q₂ Etotal",
                    metric("Campo neto", field, "N/C"), metric("Potencial en q₂", potential, "V"),
                    metric("Energía potencial", energy, "J"), metric("Dirección neta", direction, "sobre la línea"));
            setSteps(
                    "Convierte las distancias a metros: r₁ = " + Ui.number(r) + " m; r₃ = " + Ui.number(r3) + " m.",
                    "Calcula los campos con signo en q₂: E₁ = " + Ui.number(field1) + " N/C; E₃ = " + Ui.number(field3) + " N/C.",
                    "Aplica superposición: Etotal = E₁ + E₃ = " + Ui.number(field) + " N/C.",
                    "Multiplica por la carga de prueba: F₂ = q₂ Etotal; magnitud = " + Ui.number(force) + " N, " + direction + ".",
                    "El potencial es escalar: Vtotal = " + Ui.number(potential) + " V; U = q₂ Vtotal = " + Ui.number(energy) + " J.");
            updateLineGraph(q1c, q2c, q3c, r3, r, force);
            paintLine(source, test, third, Ui.read(distance), Ui.read(distance2), signedForce, field);
        } catch (NumberFormatException ex) {
            setInvalid("Escribe separaciones numéricas válidas.");
        }
    }

    private void calculatePlane(double[] charges) {
        double[] field = Field2DRenderer.sample(charges, chargeX, chargeY, testX, testY);
        double ex = field[0], ey = field[1], potential = field[2];
        boolean tooClose = false;
        for (int i = 0; i < charges.length; i++) {
            if (Math.abs(charges[i]) > 1e-10 && Math.hypot(testX - chargeX[i], testY - chargeY[i]) < .055) tooClose = true;
        }
        double magnitude = Math.hypot(ex, ey);
        double angle = Math.toDegrees(Math.atan2(ey, ex));
        setPainter2d(charges, ex, ey);
        updatePlaneGraph(charges);
        if (tooClose) {
            setInvalid("El punto P está demasiado cerca de una carga. Haz clic en otra zona del plano.");
            return;
        }
        setValidDetailColor();
        setResult("Campo eléctrico en P", Ui.number(magnitude) + " N/C",
                "P = (" + Ui.number(testX) + ", " + Ui.number(testY) + ") m · dirección " + Ui.number(angle) + "° desde +x.",
                "E⃗(P) = Σ k qᵢ r⃗ᵢ / |r⃗ᵢ|³",
                metric("Componente Eₓ", ex, "N/C"), metric("Componente Eᵧ", ey, "N/C"),
                metric("Potencial V(P)", potential, "V"), metric("Dirección", Ui.number(angle), "grados desde +x"));
        setSteps(
                "Ubica las cargas fuente: " + activeChargeSummary(charges) + ".",
                "Evalúa en P(" + Ui.number(testX) + ", " + Ui.number(testY) + ") m las componentes de cada carga con E⃗ᵢ = kqᵢr⃗ᵢ/rᵢ³.",
                "Suma las componentes: Eₓ = " + Ui.number(ex) + " N/C y Eᵧ = " + Ui.number(ey) + " N/C.",
                "La magnitud resultante es |E⃗| = √(Eₓ² + Eᵧ²) = " + Ui.number(magnitude) + " N/C; el ángulo es " + Ui.number(angle) + "°.",
                "El potencial se suma como escalar: V(P) = Σ kqᵢ/rᵢ = " + Ui.number(potential) + " V.");
    }

    private String activeChargeSummary(double[] charges) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < charges.length; i++) {
            if (Math.abs(charges[i]) < 1e-10) continue;
            if (!text.isEmpty()) text.append("; ");
            text.append("q").append(i + 1).append(" = ").append(Ui.number(charges[i])).append(" nC en (")
                    .append(Ui.number(chargeX[i])).append(", ").append(Ui.number(chargeY[i])).append(") m");
        }
        return text.isEmpty() ? "todas las cargas son cero" : text.toString();
    }

    private void updateLineGraph(double q1c, double q2c, double q3c, double r3, double currentR, double currentForce) {
        double[] x = new double[29];
        double[] y = new double[29];
        for (int i = 0; i < x.length; i++) {
            double distanceMeters = (2 + i) / 100.0;
            x[i] = 2 + i;
            y[i] = Math.abs(q2c * (K * q1c / (distanceMeters * distanceMeters) - K * q3c / (r3 * r3)));
        }
        setGraph("distancia q₁–q₂ (cm)", "|F₂| (N)", x, y, currentR * 100.0, currentForce);
    }

    private void updatePlaneGraph(double[] charges) {
        double[] x = new double[61];
        double[] y = new double[61];
        for (int i = 0; i < x.length; i++) {
            x[i] = -5 + 10.0 * i / (x.length - 1);
            double[] values = Field2DRenderer.sample(charges, chargeX, chargeY, x[i], testY);
            y[i] = Math.min(1e6, Math.hypot(values[0], values[1]));
        }
        double[] current = Field2DRenderer.sample(charges, chargeX, chargeY, testX, testY);
        setGraph("x (m) · y = " + Ui.number(testY) + " m", "|E| (N/C)", x, y,
                testX, Math.min(1e6, Math.hypot(current[0], current[1])));
    }

    private void startInteraction(Point point) {
        draggedChargeIndex = -1;
        if (experiment.combo.getSelectedIndex() != 1) return;
        for (int i = 0; i < chargeX.length; i++) {
            Point chargePoint = new Point(Field2DRenderer.screenX(chargeX[i], diagram.getSize()),
                    Field2DRenderer.screenY(chargeY[i], diagram.getSize()));
            if (chargePoint.distance(point) <= 18) {
                draggedChargeIndex = i;
                break;
            }
        }
    }

    private void dragInteraction(Point start, Point current) {
        if (experiment.combo.getSelectedIndex() == 0) {
            dragTestCharge(start, current);
            return;
        }
        if (draggedChargeIndex < 0) return;
        chargeX[draggedChargeIndex] = clamp(Field2DRenderer.worldX(current.x, diagram.getSize()));
        chargeY[draggedChargeIndex] = clamp(Field2DRenderer.worldY(current.y, diagram.getSize()));
        calculate();
    }

    private void clickOnDiagram(Point point) {
        if (experiment.combo.getSelectedIndex() != 1 || draggedChargeIndex >= 0) return;
        testX = clamp(Field2DRenderer.worldX(point.x, diagram.getSize()));
        testY = clamp(Field2DRenderer.worldY(point.y, diagram.getSize()));
        calculate();
    }

    private static double clamp(double value) {
        return Math.max(-4.8, Math.min(4.8, value));
    }

    private void setPainter2d(double[] charges, double ex, double ey) {
        diagram.setPainter((graphics, size) -> {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Field2DRenderer.paint(g, size, charges, chargeX, chargeY, testX, testY, ex, ey);
            g.dispose();
        });
    }

    private void dragTestCharge(Point start, Point current) {
        int width = diagram.getWidth();
        if (width <= 0) return;
        double d1 = Ui.read(distance);
        double fraction = 0.34 + (d1 - 2) / 28.0 * 0.27;
        int q2x = (int) (fraction * width);
        if (Math.abs(start.x - q2x) > 32 || Math.abs(start.y - 100) > 38) return;
        double next = Math.max(2, Math.min(30, 2 + (current.x / (double) width - 0.34) / 0.27 * 28));
        distance.field.setText(String.format(Locale.US, "%.1f", next));
    }

    @Override
    protected void saveCustomConfiguration(Properties properties) {
        properties.setProperty("plane.testX", Double.toString(testX));
        properties.setProperty("plane.testY", Double.toString(testY));
        for (int i = 0; i < chargeX.length; i++) {
            properties.setProperty("plane.q" + (i + 1) + ".x", Double.toString(chargeX[i]));
            properties.setProperty("plane.q" + (i + 1) + ".y", Double.toString(chargeY[i]));
        }
    }

    @Override
    protected void loadCustomConfiguration(Properties properties) {
        testX = readCoordinate(properties, "plane.testX", testX);
        testY = readCoordinate(properties, "plane.testY", testY);
        for (int i = 0; i < chargeX.length; i++) {
            chargeX[i] = clamp(readCoordinate(properties, "plane.q" + (i + 1) + ".x", chargeX[i]));
            chargeY[i] = clamp(readCoordinate(properties, "plane.q" + (i + 1) + ".y", chargeY[i]));
        }
        calculate();
    }

    private static double readCoordinate(Properties properties, String key, double fallback) {
        try {
            double value = Double.parseDouble(properties.getProperty(key, Double.toString(fallback)));
            return Double.isFinite(value) ? value : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    protected String challengeQuestion() {
        return "Si duplicas la distancia entre q₁ y q₂, ¿cómo cambia el campo de q₁ en q₂? (q₃ = 0)";
    }

    @Override
    protected String[] challengeAnswers() {
        return new String[]{"Se reduce a la cuarta parte", "Se reduce a la mitad", "Se duplica"};
    }

    @Override
    protected String challengeExplanation() {
        return "El campo de una carga puntual es inversamente proporcional a r²: al duplicar r, E queda en E/4.";
    }

    private void paintLine(double source, double test, double third, double r1, double r3, double signedForce, double field) {
        diagram.setPainter((graphics, size) -> {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double y = Math.min(111, size.height * 0.43);
            double x1 = size.width * 0.14;
            double x2 = size.width * (0.34 + (r1 - 2) / 28.0 * 0.27);
            double x3 = Math.min(size.width * 0.91, x2 + size.width * (0.18 + (r3 - 2) / 28.0 * 0.11));
            g.setColor(Theme.LINE);
            g.setStroke(new BasicStroke(1.3f));
            g.drawLine((int) x1, (int) y, (int) x3, (int) y);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.setColor(Theme.SUBTLE);
            g.drawString("r₁", (int) ((x1 + x2) / 2), (int) y - 18);
            g.drawString("r₃", (int) ((x2 + x3) / 2), (int) y - 18);
            circle(g, x1, y, sign(source), chargeColor(source));
            circle(g, x2, y, sign(test), chargeColor(test));
            circle(g, x3, y, sign(third), chargeColor(third));
            g.setColor(Theme.MUTED);
            g.drawString("q₁", (int) x1 - 8, (int) y + 42);
            g.drawString("q₂ · arrastra", (int) x2 - 29, (int) y + 42);
            g.drawString("q₃", (int) x3 - 8, (int) y + 42);
            if (signedForce != 0) {
                double fEnd = signedForce < 0 ? x2 - 43 : x2 + 43;
                arrow(g, x2, 77, fEnd, 77, Theme.YELLOW, 2.4f);
                g.setColor(Theme.YELLOW);
                g.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g.drawString("F₂", (int) (signedForce < 0 ? x2 - 66 : x2 + 46), 72);
            }
            if (field != 0) {
                boolean fieldRight = field > 0;
                double eStart = fieldRight ? x2 - 43 : x2 + 43;
                double eEnd = fieldRight ? x2 + 40 : x2 - 40;
                arrow(g, eStart, 119, eEnd, 119, Theme.TEAL, 2.0f);
                g.setColor(Theme.TEAL);
                g.drawString("E neto", (int) (fieldRight ? x2 + 42 : x2 - 77), 124);
            }
            g.setColor(Theme.SUBTLE);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("Arrastra q₂ sobre la línea para mover la carga de prueba", 15, size.height - 12);
            g.dispose();
        });
    }

    private static void circle(Graphics2D g, double x, double y, String sign, Color color) {
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 38));
        g.fillOval((int) x - 27, (int) y - 27, 54, 54);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.6f));
        g.drawOval((int) x - 22, (int) y - 22, 44, 44);
        g.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g.drawString(sign, (int) x - 7, (int) y + 8);
    }

    private static String sign(double charge) {
        return charge > 0 ? "+" : charge < 0 ? "−" : "0";
    }

    private static Color chargeColor(double charge) {
        return charge > 0 ? Theme.RED : charge < 0 ? Theme.BLUE : Theme.SUBTLE;
    }

    private static void arrow(Graphics2D g, double x1, double y1, double x2, double y2, Color color, float width) {
        g.setColor(color);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int head = 8;
        int ax = (int) (x2 - head * Math.cos(angle - Math.PI / 6));
        int ay = (int) (y2 - head * Math.sin(angle - Math.PI / 6));
        int bx = (int) (x2 - head * Math.cos(angle + Math.PI / 6));
        int by = (int) (y2 - head * Math.sin(angle + Math.PI / 6));
        g.drawLine((int) x2, (int) y2, ax, ay);
        g.drawLine((int) x2, (int) y2, bx, by);
    }
}
