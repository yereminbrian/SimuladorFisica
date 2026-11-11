package pe.educacion.electrolab.modules;

import pe.educacion.electrolab.ui.ModuleScreen;
import pe.educacion.electrolab.ui.Theme;
import pe.educacion.electrolab.ui.Ui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CapacitorScreen extends ModuleScreen {
    private static final double EPSILON_0 = 8.8541878128e-12;
    private static final Map<String, Double> DIELECTRICS = new LinkedHashMap<>();

    static {
        DIELECTRICS.put("Vacío / aire · κ = 1,0", 1.0);
        DIELECTRICS.put("Plástico · κ = 2,3", 2.3);
        DIELECTRICS.put("Papel · κ = 3,5", 3.5);
        DIELECTRICS.put("Vidrio · κ = 5,5", 5.5);
        DIELECTRICS.put("Mica · κ = 6,0", 6.0);
    }

    private final Ui.Choice experiment = Ui.choice("Experimento", "MODELO", new String[]{"Placas paralelas + dieléctrico", "Banco de dos capacitores"}, "Elige si quieres explorar un capacitor o una combinación en serie/paralelo.");
    private final Ui.Input area = Ui.input("Área de cada placa", "cm²", "100", "Área efectiva de una placa conductora.");
    private final Ui.Input gap = Ui.input("Separación d", "mm", "2", "Distancia entre placas. Puedes mover la placa derecha en el esquema.");
    private final Ui.Input voltage = Ui.input("Voltaje inicial", "V", "9", "Voltaje inicial antes de insertar el dieléctrico.");
    private final Ui.Choice material = Ui.choice("Material entre placas", "DIELÉCTRICO", DIELECTRICS.keySet().toArray(String[]::new), "La constante dieléctrica κ indica cuántas veces aumenta la capacitancia ideal.");
    private final Ui.Choice connection = Ui.choice("Condición eléctrica", "FUENTE", new String[]{"Conectado a batería", "Aislado (carga constante)"}, "Con batería, el voltaje permanece fijo. Aislado, la carga permanece fija.");
    private final Ui.Input cap1 = Ui.input("Capacitor C₁", "pF", "100", "Capacitancia del primer componente.");
    private final Ui.Input cap2 = Ui.input("Capacitor C₂", "pF", "220", "Capacitancia del segundo componente.");
    private final Ui.Input networkVoltage = Ui.input("Voltaje de la fuente", "V", "9", "Voltaje aplicado al banco de capacitores.");
    private final Ui.Choice network = Ui.choice("Conexión del banco", "RED", new String[]{"Serie", "Paralelo"}, "Compara cómo cambia la capacitancia equivalente.");
    private final JPanel experiments = new JPanel(new CardLayout());
    private final JPanel plateGrid = Ui.fieldGrid();
    private final JPanel bankGrid = Ui.fieldGrid();

    public CapacitorScreen() {
        super("02", "CAPACITORES", "Capacitancia y dieléctricos",
                "Inserta un material aislante o compara capacitores en serie y paralelo.",
                "¿Qué cambia al insertar un dieléctrico?",
                "El material se polariza y aumenta la capacitancia en un factor κ. Si la batería sigue conectada, conserva el voltaje y entrega carga adicional. Si el capacitor está aislado, conserva su carga; por eso bajan el voltaje y la energía almacenada. También puedes cambiar a un banco de capacitores y explorar conexiones en serie y paralelo.");

        registerSetting(experiment);
        JPanel configuration = new JPanel();
        configuration.setOpaque(false);
        configuration.setLayout(new javax.swing.BoxLayout(configuration, javax.swing.BoxLayout.Y_AXIS));
        configuration.add(experiment.panel);
        Ui.addGap(configuration, 8);
        configuration.add(experiments);
        settingsGrid.setLayout(new GridLayout(1, 1));
        settingsGrid.add(configuration);
        plateGrid.add(area.panel);
        plateGrid.add(gap.panel);
        plateGrid.add(voltage.panel);
        plateGrid.add(material.panel);
        plateGrid.add(connection.panel);
        bankGrid.add(cap1.panel);
        bankGrid.add(cap2.panel);
        bankGrid.add(networkVoltage.panel);
        bankGrid.add(network.panel);
        experiments.add(plateGrid, "plates");
        experiments.add(bankGrid, "bank");

        registerSetting(area);
        registerSetting(gap);
        registerSetting(voltage);
        registerSetting(material);
        registerSetting(connection);
        registerSetting(cap1);
        registerSetting(cap2);
        registerSetting(networkVoltage);
        registerSetting(network);

        gap.addSlider(1, 10, 2, "mm");
        inputListener(area, this::calculate);
        inputListener(gap, this::calculate);
        inputListener(voltage, this::calculate);
        inputListener(cap1, this::calculate);
        inputListener(cap2, this::calculate);
        inputListener(networkVoltage, this::calculate);
        comboListener(material, this::calculate);
        comboListener(connection, this::calculate);
        comboListener(network, this::calculate);
        comboListener(experiment, () -> {
            ((CardLayout) experiments.getLayout()).show(experiments, experiment.combo.getSelectedIndex() == 0 ? "plates" : "bank");
            calculate();
        });
        diagram.setDragHandler(this::dragPlate);
        calculate();
    }

    private void calculate() {
        if (experiment.combo.getSelectedIndex() == 0) {
            calculatePlates();
        } else {
            calculateBank();
        }
    }

    private void calculatePlates() {
        try {
            double a = Ui.read(area) * 1e-4;
            double d = Ui.read(gap) * 1e-3;
            double initialV = Ui.read(voltage);
            if (!Double.isFinite(a) || !Double.isFinite(d) || !Double.isFinite(initialV) || a <= 0 || d <= 0 || initialV < 0) {
                setInvalid("El área y la separación deben ser mayores que cero; el voltaje no puede ser negativo.");
                return;
            }
            double kappa = DIELECTRICS.get((String) material.combo.getSelectedItem());
            double c0 = EPSILON_0 * a / d;
            double c = kappa * c0;
            boolean connected = connection.combo.getSelectedIndex() == 0;
            double finalV = connected ? initialV : initialV / kappa;
            double charge = connected ? c * finalV : c0 * initialV;
            double field = finalV / d;
            double energy = 0.5 * c * finalV * finalV;
            setValidDetailColor();
            setResult("Capacitancia con dieléctrico", Ui.number(c) + " F", "κ = " + Ui.number(kappa) + " · " + (connected ? "la batería mantiene V" : "el capacitor conserva Q"),
                    "C = κ ε₀ A / d",
                    metric("Carga final", charge, "C"),
                    metric("Voltaje final", finalV, "V"),
                    metric("Campo entre placas", field, "V/m"),
                    metric("Energía almacenada", energy, "J"));
            setSteps(
                    "Convierte el área y la separación: A = " + Ui.number(a) + " m²; d = " + Ui.number(d) + " m.",
                    "Capacitancia sin dieléctrico: C₀ = ε₀ A/d = " + Ui.number(c0) + " F.",
                    "Incluye el material: C = κ C₀ = " + Ui.number(kappa) + " × " + Ui.number(c0) + " = " + Ui.number(c) + " F.",
                    connected ? "La batería fija V = " + Ui.number(finalV) + " V; Q = CV = " + Ui.number(charge) + " C." : "El capacitor aislado mantiene Q = " + Ui.number(charge) + " C; Vfinal = V₀/κ = " + Ui.number(finalV) + " V.",
                    "Campo E = V/d = " + Ui.number(field) + " V/m; energía U = ½CV² = " + Ui.number(energy) + " J.");
            updatePlateGraph(a, kappa, d, c);
            paintPlates(kappa, connected, finalV, Ui.read(gap));
        } catch (NumberFormatException ex) {
            setInvalid("Completa área, separación y voltaje con números válidos.");
        }
    }

    private void calculateBank() {
        try {
            double c1 = Ui.read(cap1) * 1e-12;
            double c2 = Ui.read(cap2) * 1e-12;
            double v = Ui.read(networkVoltage);
            if (!Double.isFinite(c1) || !Double.isFinite(c2) || !Double.isFinite(v) || c1 <= 0 || c2 <= 0 || v < 0) {
                setInvalid("Las capacitancias deben ser positivas y el voltaje no puede ser negativo.");
                return;
            }
            boolean parallel = network.combo.getSelectedIndex() == 1;
            double equivalent = parallel ? c1 + c2 : 1.0 / (1.0 / c1 + 1.0 / c2);
            double totalCharge = equivalent * v;
            double v1 = parallel ? v : totalCharge / c1;
            double v2 = parallel ? v : totalCharge / c2;
            double q1 = c1 * v1;
            double q2 = c2 * v2;
            double energy = 0.5 * equivalent * v * v;
            setValidDetailColor();
            setResult("Capacitancia equivalente", Ui.number(equivalent) + " F", parallel ? "En paralelo, ambos componentes tienen el mismo voltaje." : "En serie, ambos componentes almacenan la misma magnitud de carga.",
                    parallel ? "Ceq = C₁ + C₂" : "1/Ceq = 1/C₁ + 1/C₂",
                    metric("Carga total", totalCharge, "C"),
                    metric("Voltaje en C₁", v1, "V"),
                    metric("Voltaje en C₂", v2, "V"),
                    metric("Energía total", energy, "J"));
            setSteps(
                    "Convierte C₁ y C₂ de picofarads a farads: C₁ = " + Ui.number(c1) + " F; C₂ = " + Ui.number(c2) + " F.",
                    parallel ? "Suma en paralelo: Ceq = C₁ + C₂ = " + Ui.number(equivalent) + " F." : "Combina en serie: Ceq = (1/C₁ + 1/C₂)⁻¹ = " + Ui.number(equivalent) + " F.",
                    "La fuente aplica V = " + Ui.number(v) + " V; carga total Q = Ceq V = " + Ui.number(totalCharge) + " C.",
                    "Voltajes: V₁ = " + Ui.number(v1) + " V y V₂ = " + Ui.number(v2) + " V; cargas Q₁ = " + Ui.number(q1) + " C y Q₂ = " + Ui.number(q2) + " C.",
                    "Energía almacenada total U = ½CeqV² = " + Ui.number(energy) + " J.");
            updateBankGraph(c1, c2, equivalent);
            paintBank(parallel, c1, c2);
        } catch (NumberFormatException ex) {
            setInvalid("Completa C₁, C₂ y el voltaje con números válidos.");
        }
    }

    private void updatePlateGraph(double areaMeters, double kappa, double d, double current) {
        double[] x = new double[19];
        double[] y = new double[19];
        for (int i = 0; i < x.length; i++) {
            double millimeters = 1 + i * 0.5;
            x[i] = millimeters;
            y[i] = kappa * EPSILON_0 * areaMeters / (millimeters * 1e-3);
        }
        setGraph("separación d (mm)", "C (F)", x, y, d * 1e3, current);
    }

    private void updateBankGraph(double c1, double currentC2, double currentEquivalent) {
        double[] x = new double[30];
        double[] y = new double[30];
        boolean parallel = network.combo.getSelectedIndex() == 1;
        for (int i = 0; i < x.length; i++) {
            double c2 = (10 + i * 20) * 1e-12;
            x[i] = c2 * 1e12;
            y[i] = parallel ? c1 + c2 : 1.0 / (1.0 / c1 + 1.0 / c2);
        }
        setGraph("C₂ (pF)", "Ceq (F)", x, y, currentC2 * 1e12, currentEquivalent);
    }

    private void dragPlate(Point start, Point current) {
        if (experiment.combo.getSelectedIndex() != 0 || diagram.getWidth() <= 0) {
            return;
        }
        int width = diagram.getWidth();
        int leftX = (int) (width * 0.39);
        double currentGap = Ui.read(gap) * 1e-3;
        int gapPixels = (int) (24 + (currentGap * 1e3 - 1.0) / 9.0 * 72);
        int rightPlate = leftX + gapPixels;
        if (Math.abs(start.x - rightPlate) > 12 || Math.abs(start.y - 105) > 55) {
            return;
        }
        double mm = 1 + (current.x - leftX - 24) / 72.0 * 9.0;
        mm = Math.max(1, Math.min(10, mm));
        gap.field.setText(String.format(java.util.Locale.US, "%.1f", mm));
    }

    private void paintPlates(double kappa, boolean connected, double finalV, double gapMm) {
        diagram.setPainter((graphics, size) -> {
            Graphics2D g = (Graphics2D) graphics.create();
            int leftX = (int) (size.width * 0.39);
            int gapPixels = (int) (24 + (gapMm - 1.0) / 9.0 * 72);
            int rightX = leftX + gapPixels;
            int top = 54;
            int bottom = 157;
            g.setColor(Theme.BLUE);
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(leftX, top, leftX, bottom);
            g.drawLine(rightX, top, rightX, bottom);
            g.setColor(new Color(Theme.TEAL.getRed(), Theme.TEAL.getGreen(), Theme.TEAL.getBlue(), 25));
            g.fillRoundRect(leftX + 4, top + 10, Math.max(2, gapPixels - 8), bottom - top - 20, 8, 8);
            g.setColor(new Color(Theme.TEAL.getRed(), Theme.TEAL.getGreen(), Theme.TEAL.getBlue(), 150));
            g.setStroke(new BasicStroke(1.3f));
            for (int y = top + 23; y < bottom - 10; y += 25) {
                g.drawLine(leftX + 7, y, rightX - 7, y);
                g.fillOval((leftX + rightX) / 2 - 3, y - 3, 6, 6);
            }
            g.setColor(Theme.MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.drawString("+Q", leftX - 34, top + 9);
            g.drawString("−Q", rightX + 7, bottom - 2);
            g.drawString("arrastra la placa", rightX - 38, bottom + 23);
            g.setColor(Theme.TEAL);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString("κ = " + Ui.number(kappa), leftX - 14, 34);

            if (connected) {
                g.setColor(Theme.YELLOW);
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(leftX - 12, top + 14, leftX - 40, top + 14);
                g.drawLine(leftX - 40, top + 14, leftX - 40, bottom - 15);
                g.drawLine(leftX - 40, bottom - 15, leftX - 12, bottom - 15);
                g.drawOval(leftX - 47, 94, 14, 14);
                g.drawLine(leftX - 44, 101, leftX - 36, 101);
                g.drawLine(leftX - 40, 97, leftX - 40, 105);
                g.drawLine(leftX - 44, 130, leftX - 36, 130);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g.drawString("batería", leftX - 80, 179);
            } else {
                g.setColor(Theme.SUBTLE);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g.drawString("aislado · Q fija", leftX - 20, 190);
            }
            g.setColor(Theme.SUBTLE);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("d = " + Ui.number(gapMm) + " mm   ·   Vfinal = " + Ui.number(finalV) + " V", leftX - 31, 211);
            g.dispose();
        });
    }

    private void paintBank(boolean parallel, double c1, double c2) {
        diagram.setPainter((graphics, size) -> {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(Theme.BLUE);
            g.setStroke(new BasicStroke(1.8f));
            if (parallel) {
                g.drawLine(105, 55, 105, 178);
                g.drawLine(385, 55, 385, 178);
                g.drawLine(105, 70, 195, 70);
                capacitorSymbol(g, 195, 70, "C₁", c1);
                g.drawLine(253, 70, 385, 70);
                g.drawLine(105, 157, 195, 157);
                capacitorSymbol(g, 195, 157, "C₂", c2);
                g.drawLine(253, 157, 385, 157);
            } else {
                g.drawLine(95, 112, 153, 112);
                capacitorSymbol(g, 153, 112, "C₁", c1);
                g.drawLine(211, 112, 245, 112);
                capacitorSymbol(g, 245, 112, "C₂", c2);
                g.drawLine(303, 112, 386, 112);
                g.drawLine(386, 112, 386, 173);
                g.drawLine(386, 173, 95, 173);
                g.drawLine(95, 173, 95, 112);
            }
            g.setColor(Theme.MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString(parallel ? "mismo voltaje en los ramales" : "misma carga en cada capacitor", 147, 209);
            g.dispose();
        });
    }

    private static void capacitorSymbol(Graphics2D g, int x, int y, String name, double value) {
        g.setColor(Theme.YELLOW);
        g.setStroke(new BasicStroke(3f));
        g.drawLine(x + 20, y - 17, x + 20, y + 17);
        g.drawLine(x + 36, y - 17, x + 36, y + 17);
        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g.drawString(name, x + 15, y - 24);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g.drawString(Ui.number(value * 1e12) + "pF", x + 8, y + 31);
    }

    @Override
    protected String challengeQuestion() {
        return experiment.combo.getSelectedIndex() == 0
                ? "Con la batería conectada, insertas un dieléctrico. ¿Qué magnitud se mantiene constante?"
                : "¿Qué conexión produce una capacitancia equivalente mayor que C₁ + C₂?";
    }

    @Override
    protected String[] challengeAnswers() {
        return experiment.combo.getSelectedIndex() == 0
                ? new String[]{"El voltaje", "La carga", "La energía"}
                : new String[]{"Ninguna; en paralelo Ceq = C₁ + C₂", "Serie", "Ambas"};
    }

    @Override
    protected int correctAnswerIndex() {
        return experiment.combo.getSelectedIndex() == 0 ? 0 : 0;
    }

    @Override
    protected String challengeExplanation() {
        return experiment.combo.getSelectedIndex() == 0
                ? "La batería fija el voltaje; al aumentar C, aumenta también la carga almacenada."
                : "En paralelo, Ceq es exactamente C₁ + C₂; en serie, el resultado es menor que cada capacitor.";
    }
}
