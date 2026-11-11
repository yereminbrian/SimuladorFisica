package pe.educacion.electrolab.modules;

import pe.educacion.electrolab.ui.ModuleScreen;
import pe.educacion.electrolab.ui.Theme;
import pe.educacion.electrolab.ui.Ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public final class DcCircuitsScreen extends ModuleScreen {
    private final Ui.Input voltage = Ui.input("Fuente ideal", "V", "12", "Voltaje suministrado por una fuente de corriente directa.");
    private final Ui.Input r1 = Ui.input("Resistor R₁", "Ω", "100", "Resistencia del primer resistor.");
    private final Ui.Input r2 = Ui.input("Resistor R₂", "Ω", "220", "Resistencia del segundo resistor.");
    private final Ui.Input r3 = Ui.input("Resistor R₃", "Ω", "330", "Resistencia del tercer resistor.");
    private final Ui.Choice topology = Ui.choice("Conexión", "RED", new String[]{
            "Serie · R₁ + R₂ + R₃",
            "Paralelo · R₁ ∥ R₂ ∥ R₃",
            "Mixto · R₁ + (R₂ ∥ R₃)"
    }, "Elige cómo se conectan los tres resistores.");

    public DcCircuitsScreen() {
        super("04", "REDES RESISTIVAS", "Circuitos de corriente directa",
                "Compara redes en serie, paralelo y una combinación mixta.",
                "Sigue el camino de la corriente",
                "En serie hay una sola trayectoria: la corriente es igual en cada resistor y los voltajes se reparten. En paralelo todos los ramales comparten voltaje y la corriente se divide. En un circuito mixto se aplican ambas reglas; en cada nodo, la corriente que entra es igual a la que sale.");
        addSetting(voltage);
        addSetting(topology);
        addSetting(r1);
        addSetting(r2);
        addSetting(r3);
        voltage.addSlider(1, 24, 12, "V");
        inputListener(voltage, this::calculate);
        inputListener(r1, this::calculate);
        inputListener(r2, this::calculate);
        inputListener(r3, this::calculate);
        comboListener(topology, this::calculate);
        calculate();
    }

    private void calculate() {
        try {
            double v = Ui.read(voltage);
            double[] resistance = {Ui.read(r1), Ui.read(r2), Ui.read(r3)};
            if (!Double.isFinite(v) || !Double.isFinite(resistance[0]) || !Double.isFinite(resistance[1])
                    || !Double.isFinite(resistance[2]) || v < 0 || resistance[0] <= 0 || resistance[1] <= 0 || resistance[2] <= 0) {
                setInvalid("Usa un voltaje no negativo y resistencias mayores que cero.");
                return;
            }
            int type = topology.combo.getSelectedIndex();
            double equivalent;
            double[] current = new double[3];
            double[] drop = new double[3];
            if (type == 0) {
                equivalent = resistance[0] + resistance[1] + resistance[2];
                double total = v / equivalent;
                for (int i = 0; i < 3; i++) {
                    current[i] = total;
                    drop[i] = total * resistance[i];
                }
            } else if (type == 1) {
                equivalent = 1.0 / (1.0 / resistance[0] + 1.0 / resistance[1] + 1.0 / resistance[2]);
                for (int i = 0; i < 3; i++) {
                    current[i] = v / resistance[i];
                    drop[i] = v;
                }
            } else {
                double parallel = 1.0 / (1.0 / resistance[1] + 1.0 / resistance[2]);
                equivalent = resistance[0] + parallel;
                double total = v / equivalent;
                double branchVoltage = v - total * resistance[0];
                current[0] = total;
                current[1] = branchVoltage / resistance[1];
                current[2] = branchVoltage / resistance[2];
                drop[0] = total * resistance[0];
                drop[1] = branchVoltage;
                drop[2] = branchVoltage;
            }
            double totalCurrent = v / equivalent;
            double power = v * totalCurrent;
            setValidDetailColor();
            String branchText = String.format("I₁ %s A · I₂ %s A · I₃ %s A", Ui.number(current[0]), Ui.number(current[1]), Ui.number(current[2]));
            setResult("Corriente total de la fuente", Ui.number(totalCurrent) + " A", branchText,
                    type == 0 ? "Req = R₁ + R₂ + R₃" : type == 1 ? "1/Req = 1/R₁ + 1/R₂ + 1/R₃" : "Req = R₁ + (R₂ ∥ R₃)",
                    metric("Resistencia equivalente", equivalent, "Ω"),
                    metric("Potencia total", power, "W"),
                    metric("Caída en R₁", drop[0], "V"),
                    metric("Caídas R₂ / R₃", Ui.number(drop[1]) + " / " + Ui.number(drop[2]), "V"));
            setSteps(
                    type == 0 ? "En serie, Req = R₁ + R₂ + R₃ = " + Ui.number(equivalent) + " Ω." : type == 1 ? "En paralelo, 1/Req = 1/R₁ + 1/R₂ + 1/R₃; Req = " + Ui.number(equivalent) + " Ω." : "Reduce primero R₂ ∥ R₃: Rp = " + Ui.number(equivalent - resistance[0]) + " Ω; luego Req = R₁ + Rp = " + Ui.number(equivalent) + " Ω.",
                    "La corriente total es Itotal = V/Req = " + Ui.number(v) + " V / " + Ui.number(equivalent) + " Ω = " + Ui.number(totalCurrent) + " A.",
                    "Corrientes de rama: I₁ = " + Ui.number(current[0]) + " A, I₂ = " + Ui.number(current[1]) + " A e I₃ = " + Ui.number(current[2]) + " A.",
                    "Caídas de voltaje: V₁ = " + Ui.number(drop[0]) + " V, V₂ = " + Ui.number(drop[1]) + " V, V₃ = " + Ui.number(drop[2]) + " V.",
                    "Comprueba la potencia total P = V Itotal = " + Ui.number(power) + " W y la ley de nodos en la conexión correspondiente.");
            updateGraph(v, resistance, type, totalCurrent);
            paint(type, resistance, current, drop);
        } catch (NumberFormatException ex) {
            setInvalid("Completa el voltaje y las tres resistencias con números válidos.");
        }
    }

    private void updateGraph(double v, double[] resistance, int type, double current) {
        double[] x = new double[40];
        double[] y = new double[40];
        for (int i = 0; i < x.length; i++) {
            double r1Value = 10 + i * 25;
            x[i] = r1Value;
            if (type == 0) {
                y[i] = v / (r1Value + resistance[1] + resistance[2]);
            } else if (type == 1) {
                y[i] = v * (1.0 / r1Value + 1.0 / resistance[1] + 1.0 / resistance[2]);
            } else {
                double parallel = 1.0 / (1.0 / resistance[1] + 1.0 / resistance[2]);
                y[i] = v / (r1Value + parallel);
            }
        }
        setGraph("resistencia R₁ (Ω)", "corriente total (A)", x, y, Ui.read(r1), current);
    }

    @Override
    protected String challengeQuestion() {
        return "En cualquiera de estas redes, si aumentas R₁ y mantienes lo demás constante, ¿qué ocurre con la corriente por R₁?";
    }

    @Override
    protected String[] challengeAnswers() {
        return new String[]{"Disminuye", "Aumenta", "Se mantiene igual en todos los casos"};
    }

    @Override
    protected String challengeExplanation() {
        return "En serie y mixto baja la corriente total; en paralelo baja I₁ = V/R₁. La respuesta es disminuir en las tres configuraciones.";
    }

    private void paint(int type, double[] resistance, double[] current, double[] drop) {
        diagram.setPainter((graphics, size) -> {
            Graphics2D g = (Graphics2D) graphics.create();
            double scale = Math.min(1.0, size.width / 470.0);
            g.scale(scale, scale);
            g.setColor(Theme.BLUE);
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (type == 0) {
                int y = 103;
                g.drawLine(70, y, 129, y);
                resistor(g, 129, y, "R₁", resistance[0]);
                g.drawLine(193, y, 223, y);
                resistor(g, 223, y, "R₂", resistance[1]);
                g.drawLine(287, y, 317, y);
                resistor(g, 317, y, "R₃", resistance[2]);
                g.drawLine(381, y, 408, y);
                g.drawLine(408, y, 408, 164);
                g.drawLine(408, 164, 70, 164);
                g.drawLine(70, 164, 70, 128);
                g.drawLine(70, 117, 70, y);
                battery(g, 70, 117);
                g.setColor(Theme.TEAL);
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g.drawString("I = " + Ui.number(current[0]) + " A", 203, 81);
            } else if (type == 1) {
                g.drawLine(82, 70, 82, 110);
                g.drawLine(82, 121, 82, 166);
                g.drawLine(384, 70, 384, 166);
                g.drawLine(82, 70, 213, 70);
                g.drawLine(277, 70, 384, 70);
                g.drawLine(82, 117, 213, 117);
                g.drawLine(277, 117, 384, 117);
                g.drawLine(82, 166, 213, 166);
                g.drawLine(277, 166, 384, 166);
                battery(g, 82, 110);
                resistor(g, 213, 70, "R₁", resistance[0]);
                resistor(g, 213, 117, "R₂", resistance[1]);
                resistor(g, 213, 166, "R₃", resistance[2]);
                g.setColor(Theme.TEAL);
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g.drawString("I₁ " + Ui.number(current[0]) + " A", 291, 62);
                g.drawString("I₂ " + Ui.number(current[1]) + " A", 291, 111);
                g.drawString("I₃ " + Ui.number(current[2]) + " A", 291, 159);
            } else {
                g.drawLine(76, 91, 134, 91);
                resistor(g, 134, 91, "R₁", resistance[0]);
                g.drawLine(198, 91, 226, 91);
                g.drawLine(226, 91, 226, 66);
                g.drawLine(226, 91, 226, 151);
                g.drawLine(226, 66, 269, 66);
                g.drawLine(226, 151, 269, 151);
                resistor(g, 269, 66, "R₂", resistance[1]);
                resistor(g, 269, 151, "R₃", resistance[2]);
                g.drawLine(333, 66, 383, 66);
                g.drawLine(333, 151, 383, 151);
                g.drawLine(383, 66, 383, 151);
                g.drawLine(383, 151, 383, 176);
                g.drawLine(383, 176, 76, 176);
                g.drawLine(76, 176, 76, 119);
                g.drawLine(76, 108, 76, 91);
                battery(g, 76, 108);
                g.setColor(Theme.TEAL);
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g.drawString("I₂", 240, 62);
                g.drawString("I₃", 240, 147);
                g.drawString("I total", 212, 102);
            }
            g.setColor(Theme.SUBTLE);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("R₁ = " + Ui.number(resistance[0]) + " Ω  ·  caída " + Ui.number(drop[0]) + " V", 103, 207);
            g.dispose();
        });
    }

    private static void resistor(Graphics2D g, int x, int y, String label, double value) {
        g.setColor(new Color(Theme.YELLOW.getRed(), Theme.YELLOW.getGreen(), Theme.YELLOW.getBlue(), 28));
        g.fillRoundRect(x, y - 12, 64, 24, 8, 8);
        g.setColor(Theme.YELLOW);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y - 12, 64, 24, 8, 8);
        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g.drawString(label, x + 8, y + 4);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g.drawString(Ui.number(value) + "Ω", x + 28, y + 4);
    }

    private static void battery(Graphics2D g, int x, int y) {
        g.setColor(Theme.MUTED);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(x - 11, y, x + 11, y);
        g.drawLine(x - 7, y + 11, x + 7, y + 11);
        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g.drawString("+", x - 3, y - 5);
    }
}
