package pe.educacion.electrolab.ui;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Ui {
    private static final Font BASE = new Font("Segoe UI", Font.PLAIN, 13);

    private Ui() {
    }

    public static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static JLabel eyebrow(String text) {
        JLabel label = label(text.toUpperCase(Locale.ROOT), 10, true, Theme.TEAL);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        return label;
    }

    public static CardPanel card() {
        return new CardPanel(Theme.CARD, Theme.LINE, 18);
    }

    public static JPanel verticalPanel(int gap) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.putClientProperty("gap", gap);
        return panel;
    }

    public static void addGap(JPanel panel, int height) {
        panel.add(javax.swing.Box.createRigidArea(new Dimension(0, height)));
    }

    public static Input input(String title, String unit, String initialValue, String tip) {
        return new Input(title, unit, initialValue, tip);
    }

    public static Choice choice(String title, String unit, String[] options, String tip) {
        return new Choice(title, unit, options, tip);
    }

    public static void onChange(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    public static double read(Input input) {
        return Double.parseDouble(input.field.getText().trim().replace(',', '.'));
    }

    public static String number(double value) {
        if (!Double.isFinite(value)) {
            return "—";
        }
        if (Math.abs(value) >= 0.001 && Math.abs(value) < 1_000_000) {
            DecimalFormat format = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-PE")));
            format.setGroupingUsed(false);
            return format.format(value);
        }
        DecimalFormat format = new DecimalFormat("0.###E0", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-PE")));
        format.setGroupingUsed(false);
        return format.format(value);
    }

    public static String signed(double value) {
        return (value > 0 ? "+" : "") + number(value);
    }

    public static JPanel metric(String title, String value, String unit) {
        JPanel panel = new CardPanel(Theme.CARD_ALT, Theme.LINE, 12);
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
        JLabel name = label(title.toUpperCase(Locale.ROOT), 9, true, Theme.MUTED);
        JLabel amount = label(value, 17, true, Theme.TEXT);
        JLabel suffix = label(unit, 10, false, Theme.SUBTLE);
        panel.add(name);
        addGap(panel, 5);
        panel.add(amount);
        addGap(panel, 2);
        panel.add(suffix);
        panel.putClientProperty("amount", amount);
        return panel;
    }

    public static void setMetric(JPanel metric, String value, String unit) {
        Object object = metric.getClientProperty("amount");
        if (object instanceof JLabel amount) {
            amount.setText(value);
        }
        Component[] components = metric.getComponents();
        if (components.length > 2 && components[components.length - 1] instanceof JLabel suffix) {
            suffix.setText(unit);
        }
    }

    public static JPanel fieldGrid() {
        JPanel grid = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        grid.setOpaque(false);
        return grid;
    }

    public static class Input {
        public final JTextField field;
        public final JPanel panel;
        public final String title;

        private Input(String title, String unit, String initialValue, String tip) {
            this.title = title;
            panel = new CardPanel(Theme.CARD_ALT, Theme.LINE, 12);
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 11, 10, 11));
            JPanel heading = new JPanel(new java.awt.BorderLayout(6, 0));
            heading.setOpaque(false);
            JLabel name = label(title, 11, true, Theme.MUTED);
            JLabel units = label(unit, 10, false, Theme.SUBTLE);
            units.setHorizontalAlignment(SwingConstants.RIGHT);
            heading.add(name, java.awt.BorderLayout.WEST);
            heading.add(units, java.awt.BorderLayout.EAST);
            field = new JTextField(initialValue);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            field.setForeground(Theme.TEXT);
            field.setCaretColor(Theme.TEAL);
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createCompoundBorder(new RoundLineBorder(Theme.LINE, 8), BorderFactory.createEmptyBorder(7, 9, 7, 9)));
            field.setToolTipText(tip);
            panel.add(heading);
            addGap(panel, 7);
            panel.add(field);
            panel.setToolTipText(tip);
        }

        public void addSlider(int minimum, int maximum, int initial, String suffix) {
            JPanel labels = new JPanel(new java.awt.BorderLayout());
            labels.setOpaque(false);
            labels.add(label("AJUSTE RÁPIDO", 8, true, Theme.SUBTLE), java.awt.BorderLayout.WEST);
            JLabel value = label(initial + " " + suffix, 9, true, Theme.TEAL);
            labels.add(value, java.awt.BorderLayout.EAST);
            JSlider slider = new JSlider(minimum, maximum, initial);
            slider.setOpaque(false);
            slider.setForeground(Theme.TEAL);
            slider.setFocusable(false);
            slider.addChangeListener(event -> {
                int next = slider.getValue();
                value.setText(next + " " + suffix);
                if (!field.getText().equals(Integer.toString(next))) {
                    field.setText(Integer.toString(next));
                }
            });
            field.getDocument().addDocumentListener(new DocumentListener() {
                private void sync() {
                    try {
                        double entered = Double.parseDouble(field.getText().trim().replace(',', '.'));
                        if (entered == Math.rint(entered)) {
                            int next = (int) entered;
                            if (next >= minimum && next <= maximum && slider.getValue() != next) {
                                slider.setValue(next);
                            }
                        }
                    } catch (NumberFormatException ignored) {
                        // Keep the slider where it was while the user edits a partial number.
                    }
                }

                @Override
                public void insertUpdate(DocumentEvent event) {
                    sync();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    sync();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    sync();
                }
            });
            addGap(panel, 5);
            panel.add(labels);
            panel.add(slider);
            panel.putClientProperty("slider", slider);
        }
    }

    public static class Choice {
        public final JComboBox<String> combo;
        public final JPanel panel;
        public final String title;

        private Choice(String title, String unit, String[] options, String tip) {
            this.title = title;
            panel = new CardPanel(Theme.CARD_ALT, Theme.LINE, 12);
            panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 11, 10, 11));
            JPanel heading = new JPanel(new java.awt.BorderLayout(6, 0));
            heading.setOpaque(false);
            JLabel name = label(title, 11, true, Theme.MUTED);
            JLabel units = label(unit, 10, false, Theme.SUBTLE);
            units.setHorizontalAlignment(SwingConstants.RIGHT);
            heading.add(name, java.awt.BorderLayout.WEST);
            heading.add(units, java.awt.BorderLayout.EAST);
            combo = new JComboBox<>(options);
            combo.setFont(BASE);
            combo.setForeground(Theme.TEXT);
            combo.setBackground(Color.WHITE);
            combo.setFocusable(false);
            combo.setToolTipText(tip);
            panel.add(heading);
            addGap(panel, 7);
            panel.add(combo);
            panel.setToolTipText(tip);
        }
    }

    public static class CardPanel extends JPanel {
        private final Color fill;
        private final Color outline;
        private final int radius;

        public CardPanel(Color fill, Color outline, int radius) {
            this.fill = fill;
            this.outline = outline;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setStroke(new BasicStroke(1f));
            g.setColor(outline);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundLineBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        private RoundLineBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }

    public static class DiagramPanel extends JPanel {
        private BiConsumer<Graphics2D, Dimension> painter = (g, size) -> {
        };
        private BiConsumer<Point, Point> dragHandler = (start, current) -> {
        };
        private Consumer<Point> pressHandler = point -> {
        };
        private Consumer<Point> clickHandler = point -> {
        };
        private Runnable dragEndHandler = () -> {
        };
        private Point dragStart;
        private boolean dragged;

        public DiagramPanel() {
            setPreferredSize(new Dimension(500, 255));
            setMinimumSize(new Dimension(300, 220));
            setOpaque(false);
            java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent event) {
                    dragStart = event.getPoint();
                    dragged = false;
                    pressHandler.accept(dragStart);
                }

                @Override
                public void mouseDragged(java.awt.event.MouseEvent event) {
                    if (dragStart != null) {
                        if (dragStart.distance(event.getPoint()) > 4) {
                            dragged = true;
                        }
                        dragHandler.accept(dragStart, event.getPoint());
                    }
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent event) {
                    if (dragStart != null && !dragged) {
                        clickHandler.accept(event.getPoint());
                    }
                    dragStart = null;
                    dragEndHandler.run();
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
            setToolTipText("Arrastra los elementos cuando el simulador lo permita.");
        }

        public void setPainter(BiConsumer<Graphics2D, Dimension> painter) {
            this.painter = painter;
            repaint();
        }

        public void setDragHandler(BiConsumer<Point, Point> dragHandler) {
            this.dragHandler = dragHandler;
        }

        public void setPressHandler(Consumer<Point> pressHandler) {
            this.pressHandler = pressHandler;
        }

        public void setClickHandler(Consumer<Point> clickHandler) {
            this.clickHandler = clickHandler;
        }

        public void setDragEndHandler(Runnable dragEndHandler) {
            this.dragEndHandler = dragEndHandler;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            painter.accept(g, getSize());
            g.dispose();
        }
    }

    public static class PlotPanel extends JPanel {
        private String xLabel = "x";
        private String yLabel = "y";
        private double[] x = new double[0];
        private double[] y = new double[0];
        private double markerX;
        private double markerY;

        public PlotPanel() {
            setPreferredSize(new Dimension(500, 255));
            setMinimumSize(new Dimension(300, 220));
            setOpaque(false);
        }

        public void setData(String xLabel, String yLabel, double[] x, double[] y, double markerX, double markerY) {
            this.xLabel = xLabel;
            this.yLabel = yLabel;
            this.x = x.clone();
            this.y = y.clone();
            this.markerX = markerX;
            this.markerY = markerY;
            repaint();
        }

        public String[][] dataRows() {
            String[][] rows = new String[x.length][4];
            for (int i = 0; i < x.length; i++) {
                rows[i] = new String[]{"Punto de gráfica", xLabel, Ui.number(x[i]), yLabel + " = " + Ui.number(y[i])};
            }
            return rows;
        }

        public void clear() {
            x = new double[0];
            y = new double[0];
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 55;
            int right = getWidth() - 20;
            int top = 23;
            int bottom = getHeight() - 40;
            if (x.length < 2 || x.length != y.length || right <= left || bottom <= top) {
                g.setColor(Theme.MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.drawString("Ingresa valores para ver la gráfica.", left, getHeight() / 2);
                g.dispose();
                return;
            }
            double minX = x[0], maxX = x[0], minY = y[0], maxY = y[0];
            for (int i = 1; i < x.length; i++) {
                minX = Math.min(minX, x[i]);
                maxX = Math.max(maxX, x[i]);
                minY = Math.min(minY, y[i]);
                maxY = Math.max(maxY, y[i]);
            }
            if (minY == maxY) {
                minY -= 1;
                maxY += 1;
            }
            double padding = (maxY - minY) * 0.12;
            minY -= padding;
            maxY += padding;

            g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            for (int tick = 0; tick <= 4; tick++) {
                int yy = bottom - tick * (bottom - top) / 4;
                g.setColor(new Color(229, 235, 242));
                g.drawLine(left, yy, right, yy);
                double value = minY + (maxY - minY) * tick / 4.0;
                g.setColor(Theme.SUBTLE);
                g.drawString(Ui.number(value), 3, yy + 3);
            }
            g.setColor(Theme.LINE);
            g.drawLine(left, top, left, bottom);
            g.drawLine(left, bottom, right, bottom);

            g.setColor(Theme.TEAL);
            g.setStroke(new BasicStroke(2.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int previousX = 0;
            int previousY = 0;
            for (int i = 0; i < x.length; i++) {
                int px = left + (int) ((x[i] - minX) / (maxX - minX) * (right - left));
                int py = bottom - (int) ((y[i] - minY) / (maxY - minY) * (bottom - top));
                if (i > 0) {
                    g.drawLine(previousX, previousY, px, py);
                }
                previousX = px;
                previousY = py;
            }
            int mx = left + (int) ((markerX - minX) / (maxX - minX) * (right - left));
            int my = bottom - (int) ((markerY - minY) / (maxY - minY) * (bottom - top));
            mx = Math.max(left, Math.min(right, mx));
            my = Math.max(top, Math.min(bottom, my));
            g.setColor(Theme.YELLOW);
            g.fillOval(mx - 5, my - 5, 10, 10);
            g.setColor(Theme.TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g.drawString("valor actual", Math.max(left, Math.min(right - 72, mx + 8)), Math.max(top + 10, my - 8));
            g.setColor(Theme.MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString(xLabel, (left + right) / 2 - 24, getHeight() - 12);
            g.drawString(yLabel, 3, top - 6);
            g.dispose();
        }
    }
}
