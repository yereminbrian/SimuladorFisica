package pe.educacion.electrolab.modules;

import pe.educacion.electrolab.ui.ModuleScreen;
import pe.educacion.electrolab.ui.Theme;
import pe.educacion.electrolab.ui.Ui;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;

public final class CurrentScreen extends ModuleScreen {
    private final Ui.Input emf = Ui.input("Fuerza electromotriz ε", "V", "12", "Energía que la fuente entrega por cada coulomb de carga.");
    private final Ui.Input load = Ui.input("Resistencia de carga R", "Ω", "10", "Resistencia del dispositivo conectado a la fuente.");
    private final Ui.Input internal = Ui.input("Resistencia interna r", "Ω", "1", "Resistencia interna de la batería o generador.");
    private final Ui.Input duration = Ui.input("Tiempo transcurrido", "s", "5", "Intervalo durante el que circula la corriente.");

    public CurrentScreen() {
        super("03", "CORRIENTE Y FEM", "Corriente, resistencia y fem",
                "Modela una fuente real y observa el voltaje que llega a la carga.",
                "De la fuente al circuito",
                "La corriente I mide cuánta carga atraviesa una sección por unidad de tiempo. Una fuente real tiene resistencia interna: parte de la fem cae dentro de la fuente cuando circula corriente. La resistencia de la carga convierte energía eléctrica en calor, luz o movimiento.");
        addSetting(emf);
        addSetting(load);
        addSetting(internal);
        addSetting(duration);
        load.addSlider(1, 200, 10, "Ω");
        inputListener(emf, this::calculate);
        inputListener(load, this::calculate);
        inputListener(internal, this::calculate);
        inputListener(duration, this::calculate);
        calculate();
    }

    private void calculate() {
        try {
            double e = Ui.read(emf);
            double r = Ui.read(load);
            double smallR = Ui.read(internal);
            double t = Ui.read(duration);
            if (!Double.isFinite(e) || !Double.isFinite(r) || !Double.isFinite(smallR) || !Double.isFinite(t)
                    || e < 0 || r <= 0 || smallR < 0 || t < 0) {
                setInvalid("ε y t no pueden ser negativos; R debe ser positiva y r no puede ser negativa.");
                return;
            }
            double current = e / (r + smallR);
            double terminal = e - current * smallR;
            double charge = current * t;
            double power = current * current * r;
            double internalPower = current * current * smallR;
            setValidDetailColor();
            setResult("Corriente del circuito", Ui.number(current) + " A", "La corriente convencional fluye desde el terminal positivo.",
                    "I = ε / (R + r)   ·   Vterminal = ε − I r",
                    metric("Voltaje terminal", terminal, "V"),
                    metric("Carga transferida", charge, "C"),
                    metric("Potencia en R", power, "W"),
                    metric("Pérdida interna", internalPower, "W"));
            setSteps(
                    "Suma las resistencias de lazo: Rtotal = R + r = " + Ui.number(r + smallR) + " Ω.",
                    "Calcula la corriente: I = ε/Rtotal = " + Ui.number(e) + " V / " + Ui.number(r + smallR) + " Ω = " + Ui.number(current) + " A.",
                    "La caída interna es Ir = " + Ui.number(current * smallR) + " V; por eso Vterminal = ε − Ir = " + Ui.number(terminal) + " V.",
                    "En " + Ui.number(t) + " s pasa una carga Q = It = " + Ui.number(charge) + " C.",
                    "La potencia en la carga es I²R = " + Ui.number(power) + " W; la pérdida interna es I²r = " + Ui.number(internalPower) + " W.");
            updateGraph(e, smallR, r, current);
            paint(current, terminal);
        } catch (NumberFormatException ex) {
            setInvalid("Completa los cuatro valores con números válidos.");
        }
    }

    private void updateGraph(double e, double internalR, double currentR, double current) {
        double[] x = new double[40];
        double[] y = new double[40];
        for (int i = 0; i < x.length; i++) {
            double resistance = 1 + i * 5;
            x[i] = resistance;
            y[i] = e / (resistance + internalR);
        }
        setGraph("resistencia de carga R (Ω)", "corriente I (A)", x, y, currentR, current);
    }

    @Override
    protected String challengeQuestion() {
        return "Si mantienes ε y r fijas pero aumentas la resistencia de carga R, ¿qué ocurre con la corriente?";
    }

    @Override
    protected String[] challengeAnswers() {
        return new String[]{"Disminuye", "Aumenta", "No cambia"};
    }

    @Override
    protected String challengeExplanation() {
        return "I = ε/(R+r); al aumentar R, aumenta el denominador y disminuye I.";
    }

    private void paint(double current, double terminal) {
        diagram.setPainter((graphics, size) -> {
            Graphics2D g = (Graphics2D) graphics.create();
            double scale = Math.min(1.0, size.width / 470.0);
            g.scale(scale, scale);
            int left = 104;
            int right = 352;
            int top = 61;
            int bottom = 161;
            g.setColor(Theme.BLUE);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(left, top, right, top);
            g.drawLine(right, top, right, bottom);
            g.drawLine(right, bottom, left, bottom);
            g.drawLine(left, bottom, left, 124);
            g.drawLine(left, 99, left, top);

            g.setColor(Theme.MUTED);
            g.setStroke(new BasicStroke(2.4f));
            g.drawLine(left - 12, 102, left + 12, 102);
            g.drawLine(left - 7, 119, left + 7, 119);
            g.setColor(Theme.TEAL);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString("ε", left - 4, 94);
            g.setColor(Theme.MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("r", left + 19, 114);

            g.setColor(Theme.YELLOW);
            g.fillRoundRect(229, top - 12, 55, 24, 8, 8);
            g.setColor(Theme.YELLOW.darker());
            g.drawRoundRect(229, top - 12, 55, 24, 8, 8);
            g.setColor(new java.awt.Color(20, 30, 46));
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString("R", 252, top + 4);

            arrow(g, 144, bottom, 196, bottom, Theme.TEAL);
            g.setColor(Theme.TEAL);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString("I = " + Ui.number(current) + " A", 150, bottom + 23);
            g.setColor(Theme.MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("Vterminal = " + Ui.number(terminal) + " V", 227, top + 33);
            g.dispose();
        });
    }

    private static void arrow(Graphics2D g, int x1, int y1, int x2, int y2, java.awt.Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, y2, x2 - 8, y2 - 4);
        g.drawLine(x2, y2, x2 - 8, y2 + 4);
    }
}
