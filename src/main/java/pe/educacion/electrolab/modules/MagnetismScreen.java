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

public final class MagnetismScreen extends ModuleScreen {
    private final Ui.Choice mode = Ui.choice("Objeto en movimiento", "MODELO", new String[]{"Partícula cargada", "Conductor con corriente"}, "Escoge qué objeto está dentro del campo magnético.");
    private final Ui.Input field = Ui.input("Campo magnético B", "mT", "50", "Magnitud del campo magnético, en militeslas.");
    private final Ui.Input angle = Ui.input("Ángulo θ", "grados", "90", "Ángulo entre la velocidad/corriente y el campo; usa 0 a 180°.");
    private final Ui.Input charge = Ui.input("Carga q", "μC", "1", "Carga de la partícula. El signo invierte la dirección de la fuerza.");
    private final Ui.Input speed = Ui.input("Rapidez v", "m/s", "1000", "Rapidez de la partícula cargada.");
    private final Ui.Input current = Ui.input("Corriente I", "A", "2", "Corriente convencional en el conductor; el signo fija su sentido.");
    private final Ui.Input length = Ui.input("Longitud dentro del campo ℓ", "cm", "10", "Longitud del tramo recto del conductor inmerso en el campo.");
    private final JPanel modeCards = new JPanel(new CardLayout());

    public MagnetismScreen() {
        super("05", "MAGNETISMO", "Campo magnético y fuerzas magnéticas",
                "Prueba cómo influyen la intensidad del campo y el ángulo de movimiento.",
                "La dirección también importa",
                "La fuerza magnética depende de la carga en movimiento y es perpendicular al movimiento y al campo. Una carga negativa invierte el sentido que indica la regla de la mano derecha. En un conductor, la corriente convencional cumple un papel análogo. Los imanes generan campos cuyas líneas externas van del polo norte al polo sur.");
        JPanel particleCard = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        particleCard.setOpaque(false);
        particleCard.add(charge.panel);
        particleCard.add(speed.panel);
        JPanel wireCard = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        wireCard.setOpaque(false);
        wireCard.add(current.panel);
        wireCard.add(length.panel);
        modeCards.setOpaque(false);
        modeCards.add(particleCard, "particle");
        modeCards.add(wireCard, "wire");

        addSetting(mode);
        addSetting(field);
        addSetting(angle);
        registerSetting(charge);
        registerSetting(speed);
        registerSetting(current);
        registerSetting(length);
        JPanel configuration = new JPanel();
        configuration.setOpaque(false);
        configuration.setLayout(new javax.swing.BoxLayout(configuration, javax.swing.BoxLayout.Y_AXIS));
        JPanel general = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        general.setOpaque(false);
        general.add(mode.panel);
        general.add(field.panel);
        general.add(angle.panel);
        configuration.add(general);
        Ui.addGap(configuration, 8);
        configuration.add(modeCards);
        settingsGrid.setLayout(new java.awt.GridLayout(1, 1));
        settingsGrid.add(configuration);

        angle.addSlider(0, 180, 90, "°");
        inputListener(field, this::calculate);
        inputListener(angle, this::calculate);
        inputListener(charge, this::calculate);
        inputListener(speed, this::calculate);
        inputListener(current, this::calculate);
        inputListener(length, this::calculate);
        comboListener(mode, () -> {
            ((CardLayout) modeCards.getLayout()).show(modeCards, mode.combo.getSelectedIndex() == 0 ? "particle" : "wire");
            calculate();
        });
        calculate();
    }

    private void calculate() {
        try {
            double b = Ui.read(field) * 1e-3;
            double theta = Ui.read(angle);
            if (!Double.isFinite(b) || !Double.isFinite(theta) || b < 0 || theta < 0 || theta > 180) {
                setInvalid("B debe ser no negativo y θ debe estar entre 0° y 180°.");
                return;
            }
            double perpendicular = b * Math.sin(Math.toRadians(theta));
            boolean particle = mode.combo.getSelectedIndex() == 0;
            double magnitude;
            double directionFactor;
            String equation;
            String objectLabel;
            if (particle) {
                double q = Ui.read(charge) * 1e-6;
                double v = Ui.read(speed);
                if (!Double.isFinite(q) || !Double.isFinite(v) || v < 0) {
                    setInvalid("La rapidez debe ser no negativa y la carga debe ser un número válido.");
                    return;
                }
                magnitude = Math.abs(q) * v * perpendicular;
                directionFactor = Math.signum(q);
                equation = "F = |q| v B sen(θ)";
                objectLabel = "partícula · q = " + Ui.signed(Ui.read(charge)) + " μC";
            } else {
                double i = Ui.read(current);
                double l = Ui.read(length) / 100.0;
                if (!Double.isFinite(i) || !Double.isFinite(l) || l <= 0) {
                    setInvalid("La longitud debe ser mayor que cero; la corriente debe ser un número válido.");
                    return;
                }
                magnitude = Math.abs(i) * l * perpendicular;
                directionFactor = Math.signum(i);
                equation = "F = |I| ℓ B sen(θ)";
                objectLabel = "conductor · I = " + Ui.signed(i) + " A";
            }
            boolean noForce = magnitude == 0.0;
            String direction = noForce ? "Sin fuerza: los vectores son paralelos o falta carga/corriente." :
                    directionFactor > 0 ? "La fuerza sale de la pantalla (⊙), regla de la mano derecha." :
                            "La fuerza entra en la pantalla (⊗), sentido invertido.";
            setValidDetailColor();
            setResult("Fuerza magnética", Ui.number(magnitude) + " N", direction,
                    equation,
                    metric("Campo perpendicular", perpendicular, "T"),
                    metric("Ángulo", Ui.number(theta), "°"),
                    metric("Dirección", noForce ? "—" : directionFactor > 0 ? "⊙" : "⊗", "respecto a la pantalla"),
                    metric("Modelo", particle ? "Carga" : "Alambre", objectLabel));
            setSteps(
                    particle ? "Para la partícula, |q| = " + Ui.number(Math.abs(Ui.read(charge) * 1e-6)) + " C y v = " + Ui.number(Ui.read(speed)) + " m/s." : "Para el conductor, |I| = " + Ui.number(Math.abs(Ui.read(current))) + " A y ℓ = " + Ui.number(Ui.read(length) / 100.0) + " m.",
                    "Convierte el campo: B = " + Ui.number(b) + " T; su componente perpendicular es B sen θ = " + Ui.number(perpendicular) + " T.",
                    "Sustituye en la relación vectorial: " + equation + "; la magnitud resultante es " + Ui.number(magnitude) + " N.",
                    noForce ? "Como los vectores son paralelos, sen θ = 0 y no hay fuerza magnética." : "La regla de la mano derecha da una fuerza " + (directionFactor > 0 ? "hacia +z, saliendo de la pantalla." : "hacia −z, entrando en la pantalla."));
            updateGraph(particle ? Math.abs(Ui.read(charge) * 1e-6) * Ui.read(speed) : Math.abs(Ui.read(current)) * Ui.read(length) / 100.0, b, theta, magnitude);
            paint(theta, directionFactor, particle, noForce);
        } catch (NumberFormatException ex) {
            setInvalid("Completa los valores del modelo elegido con números válidos.");
        }
    }

    private void updateGraph(double factor, double b, double currentTheta, double currentForce) {
        double[] x = new double[19];
        double[] y = new double[19];
        for (int i = 0; i < x.length; i++) {
            double degrees = i * 10;
            x[i] = degrees;
            y[i] = factor * b * Math.sin(Math.toRadians(degrees));
        }
        setGraph("ángulo θ (°)", "fuerza F (N)", x, y, currentTheta, currentForce);
    }

    @Override
    protected String challengeQuestion() {
        return "Si la velocidad (o la corriente) queda paralela al campo magnético, ¿qué magnitud tiene la fuerza?";
    }

    @Override
    protected String[] challengeAnswers() {
        return new String[]{"Cero", "Máxima", "Depende solo de la masa"};
    }

    @Override
    protected String challengeExplanation() {
        return "F contiene sen θ; para θ = 0° o 180°, sen θ = 0 y la fuerza magnética desaparece.";
    }

    private void paint(double theta, double direction, boolean particle, boolean noForce) {
        diagram.setPainter((graphics, size) -> {
            Graphics2D g = (Graphics2D) graphics.create();
            double scale = Math.min(1.0, size.width / 470.0);
            g.scale(scale, scale);
            int originX = 182;
            int originY = 128;
            double rad = Math.toRadians(theta);
            int bX = originX + (int) (90 * Math.cos(rad));
            int bY = originY - (int) (90 * Math.sin(rad));
            int zX = originX + 76;
            int zY = originY + 55;

            g.setColor(Theme.SUBTLE);
            g.setStroke(new BasicStroke(1.1f));
            g.drawLine(62, originY, 397, originY);
            g.drawLine(originX, 201, originX, 51);
            g.drawLine(originX, originY, zX, zY);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g.drawString("x", 401, originY + 3);
            g.drawString("y", originX + 4, 50);
            g.drawString("z · sale", zX + 4, zY + 3);
            g.setColor(Theme.TEXT);
            g.fillOval(originX - 5, originY - 5, 10, 10);

            arrow(g, originX, originY, originX + 117, originY, Theme.BLUE, 2.5f);
            g.setColor(Theme.BLUE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString(particle ? "v" : "I", originX + 108, originY + 20);

            arrow(g, originX, originY, bX, bY, Theme.TEAL, 2.5f);
            g.setColor(Theme.TEAL);
            g.drawString("B", bX + 5, bY - 4);

            g.setColor(Theme.SUBTLE);
            g.setStroke(new BasicStroke(1.1f));
            g.drawArc(originX - 30, originY - 30, 60, 60, 0, (int) theta);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("θ", originX + 36, originY - 11);

            if (!noForce) {
                int sign = direction > 0 ? 1 : -1;
                int fx = originX + sign * 76;
                int fy = originY + sign * 55;
                arrow(g, originX, originY, fx, fy, direction > 0 ? Theme.YELLOW : Theme.RED, 2.5f);
                g.setColor(direction > 0 ? Theme.YELLOW : Theme.RED);
                g.setStroke(new BasicStroke(1.8f));
                g.drawOval(fx - 12, fy - 12, 24, 24);
                if (direction > 0) {
                    g.fillOval(fx - 3, fy - 3, 6, 6);
                } else {
                    g.drawLine(fx - 5, fy - 5, fx + 5, fy + 5);
                    g.drawLine(fx - 5, fy + 5, fx + 5, fy - 5);
                }
                g.setColor(Theme.MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g.drawString("F", fx + 7, fy - 8);
            }
            g.setColor(Theme.MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString("⊙ +z · ⊗ −z", 313, 189);
            g.setColor(Theme.SUBTLE);
            g.drawString("Vista 3D: " + (particle ? "v" : "I") + " × B = F", 23, 221);
            g.dispose();
        });
    }

    private static void arrow(Graphics2D g, int x1, int y1, int x2, int y2, Color color, float width) {
        g.setColor(color);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int head = 9;
        int ax = (int) (x2 - head * Math.cos(angle - Math.PI / 6));
        int ay = (int) (y2 - head * Math.sin(angle - Math.PI / 6));
        int bx = (int) (x2 - head * Math.cos(angle + Math.PI / 6));
        int by = (int) (y2 - head * Math.sin(angle + Math.PI / 6));
        g.drawLine(x2, y2, ax, ay);
        g.drawLine(x2, y2, bx, by);
    }
}
