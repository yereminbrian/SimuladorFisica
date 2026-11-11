package pe.educacion.electrolab.modules;

import pe.educacion.electrolab.ui.Theme;
import pe.educacion.electrolab.ui.Ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Draws a small two-dimensional electrostatics laboratory in SI coordinates. */
final class Field2DRenderer {
    private static final double K = 8.9875517923e9;
    private static final double MIN = -5.0;
    private static final double MAX = 5.0;
    private static final int LEFT = 35;
    private static final int RIGHT = 18;
    private static final int TOP = 31;
    private static final int BOTTOM = 31;

    private Field2DRenderer() {
    }

    static int screenX(double x, Dimension size) {
        return LEFT + (int) Math.round((x - MIN) / (MAX - MIN) * (size.width - LEFT - RIGHT));
    }

    static int screenY(double y, Dimension size) {
        return size.height - BOTTOM - (int) Math.round((y - MIN) / (MAX - MIN) * (size.height - TOP - BOTTOM));
    }

    static double worldX(int x, Dimension size) {
        return MIN + (x - LEFT) / (double) Math.max(1, size.width - LEFT - RIGHT) * (MAX - MIN);
    }

    static double worldY(int y, Dimension size) {
        return MIN + (size.height - BOTTOM - y) / (double) Math.max(1, size.height - TOP - BOTTOM) * (MAX - MIN);
    }

    /** Returns Ex, Ey and V for charges entered in nC at positions entered in metres. */
    static double[] sample(double[] chargesNc, double[] xs, double[] ys, double x, double y) {
        double ex = 0;
        double ey = 0;
        double potential = 0;
        for (int i = 0; i < chargesNc.length; i++) {
            double dx = x - xs[i];
            double dy = y - ys[i];
            double r2 = Math.max(dx * dx + dy * dy, 0.0025);
            double r = Math.sqrt(r2);
            double q = chargesNc[i] * 1e-9;
            double factor = K * q / (r2 * r);
            ex += factor * dx;
            ey += factor * dy;
            potential += K * q / r;
        }
        return new double[]{ex, ey, potential};
    }

    static void paint(Graphics2D g, Dimension size, double[] chargesNc, double[] xs, double[] ys,
                      double testX, double testY, double ex, double ey) {
        if (size.width < 80 || size.height < 80) return;
        drawGrid(g, size);
        drawEquipotentials(g, size, chargesNc, xs, ys);
        drawFieldLines(g, size, chargesNc, xs, ys);
        drawTestVector(g, size, testX, testY, ex, ey);
        drawCharges(g, size, chargesNc, xs, ys);
        drawLegend(g);
    }

    private static void drawGrid(Graphics2D g, Dimension size) {
        int right = size.width - RIGHT;
        int bottom = size.height - BOTTOM;
        g.setStroke(new BasicStroke(1f));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        for (int value = -5; value <= 5; value++) {
            int x = screenX(value, size);
            int y = screenY(value, size);
            g.setColor(value == 0 ? Theme.LINE : new Color(234, 239, 245));
            g.drawLine(x, TOP, x, bottom);
            g.drawLine(LEFT, y, right, y);
            g.setColor(Theme.SUBTLE);
            if (value != 0) {
                g.drawString(Integer.toString(value), x - 3, bottom + 12);
                g.drawString(Integer.toString(value), 17, y + 3);
            }
        }
        g.setColor(Theme.MUTED);
        g.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g.drawString("y (m)", 2, TOP - 7);
        g.drawString("x (m)", right - 18, size.height - 4);
    }

    private static void drawEquipotentials(Graphics2D g, Dimension size, double[] q, double[] xs, double[] ys) {
        int nx = 40;
        int ny = 28;
        double[][] potential = new double[ny + 1][nx + 1];
        List<Double> absolute = new ArrayList<>((nx + 1) * (ny + 1));
        for (int row = 0; row <= ny; row++) {
            double y = MAX - row * (MAX - MIN) / ny;
            for (int col = 0; col <= nx; col++) {
                double x = MIN + col * (MAX - MIN) / nx;
                boolean singular = false;
                for (int i = 0; i < q.length; i++) {
                    if (Math.abs(q[i]) > 1e-10 && Math.hypot(x - xs[i], y - ys[i]) < .22) {
                        singular = true;
                        break;
                    }
                }
                double v = singular ? Double.NaN : sample(q, xs, ys, x, y)[2];
                potential[row][col] = v;
                if (Double.isFinite(v)) absolute.add(Math.min(1_000_000, Math.abs(v)));
            }
        }
        if (absolute.size() < 10) return;
        absolute.sort(Double::compareTo);
        double maxAbs = absolute.get((int) (absolute.size() * .82));
        if (maxAbs < 1e-9 || !Double.isFinite(maxAbs)) return;
        maxAbs = Math.min(maxAbs, 100_000);
        double[] fractions = {.16, .32, .5, .68, .84};
        for (double fraction : fractions) {
            double level = maxAbs * fraction;
            drawContour(g, size, potential, nx, ny, level, new Color(59, 101, 214, 116));
            drawContour(g, size, potential, nx, ny, -level, new Color(203, 67, 82, 116));
        }
    }

    private static void drawContour(Graphics2D g, Dimension size, double[][] v, int nx, int ny,
                                    double level, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(.9f));
        for (int row = 0; row < ny; row++) {
            for (int col = 0; col < nx; col++) {
                double[] values = {v[row][col], v[row][col + 1], v[row + 1][col + 1], v[row + 1][col]};
                boolean valid = true;
                for (double value : values) valid &= Double.isFinite(value);
                if (!valid) continue;
                double x0 = MIN + col * (MAX - MIN) / nx;
                double x1 = MIN + (col + 1) * (MAX - MIN) / nx;
                double y0 = MAX - row * (MAX - MIN) / ny;
                double y1 = MAX - (row + 1) * (MAX - MIN) / ny;
                double[][] points = {{x0, y0}, {x1, y0}, {x1, y1}, {x0, y1}};
                int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 0}};
                double[][] crossings = new double[4][2];
                int count = 0;
                for (int[] edge : edges) {
                    int a = edge[0], b = edge[1];
                    double va = values[a], vb = values[b];
                    if ((va < level && vb >= level) || (vb < level && va >= level)) {
                        double t = (level - va) / (vb - va);
                        crossings[count][0] = points[a][0] + t * (points[b][0] - points[a][0]);
                        crossings[count][1] = points[a][1] + t * (points[b][1] - points[a][1]);
                        count++;
                    }
                }
                if (count == 2) {
                    g.drawLine(screenX(crossings[0][0], size), screenY(crossings[0][1], size),
                            screenX(crossings[1][0], size), screenY(crossings[1][1], size));
                } else if (count == 4) {
                    g.drawLine(screenX(crossings[0][0], size), screenY(crossings[0][1], size),
                            screenX(crossings[1][0], size), screenY(crossings[1][1], size));
                    g.drawLine(screenX(crossings[2][0], size), screenY(crossings[2][1], size),
                            screenX(crossings[3][0], size), screenY(crossings[3][1], size));
                }
            }
        }
    }

    private static void drawFieldLines(Graphics2D g, Dimension size, double[] q, double[] xs, double[] ys) {
        g.setColor(new Color(0, 132, 119, 150));
        g.setStroke(new BasicStroke(1.25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        boolean hasPositive = Arrays.stream(q).anyMatch(value -> value > 0);
        for (int i = 0; i < q.length; i++) {
            if (Math.abs(q[i]) < 1e-10 || (hasPositive && q[i] < 0)) continue;
            double direction = q[i] > 0 ? 1 : -1;
            int seeds = Math.max(8, Math.min(20, (int) Math.round(10 + 2 * Math.sqrt(Math.abs(q[i])))));
            for (int seed = 0; seed < seeds; seed++) {
                double angle = 2 * Math.PI * (seed + .5 * (i % 2)) / seeds;
                double x = xs[i] + .19 * Math.cos(angle);
                double y = ys[i] + .19 * Math.sin(angle);
                Path2D path = new Path2D.Double();
                path.moveTo(screenX(x, size), screenY(y, size));
                List<Point> points = new ArrayList<>();
                points.add(new Point(screenX(x, size), screenY(y, size)));
                double traveled = 0;
                for (int step = 0; step < 340; step++) {
                    double[] e1 = sample(q, xs, ys, x, y);
                    double magnitude = Math.hypot(e1[0], e1[1]);
                    if (!Double.isFinite(magnitude) || magnitude < 1e-8) break;
                    double dx = direction * e1[0] / magnitude * .055;
                    double dy = direction * e1[1] / magnitude * .055;
                    double[] mid = sample(q, xs, ys, x + dx * .5, y + dy * .5);
                    double midMag = Math.hypot(mid[0], mid[1]);
                    if (midMag < 1e-8 || !Double.isFinite(midMag)) break;
                    x += direction * mid[0] / midMag * .055;
                    y += direction * mid[1] / midMag * .055;
                    traveled += .055;
                    points.add(new Point(screenX(x, size), screenY(y, size)));
                    path.lineTo(screenX(x, size), screenY(y, size));
                    if (Math.abs(x) > 5.2 || Math.abs(y) > 5.2) break;
                    boolean reachedCharge = false;
                    for (int j = 0; j < q.length; j++) {
                        if (Math.abs(q[j]) < 1e-10 || Math.hypot(x - xs[j], y - ys[j]) > .14) continue;
                        if (traveled > .3 && (j != i || q[j] * direction < 0 || traveled > .6)) reachedCharge = true;
                    }
                    if (reachedCharge) break;
                }
                g.draw(path);
                if (points.size() > 12) {
                    int at = Math.min(points.size() - 2, (int) (points.size() * .62));
                    Point a = points.get(at);
                    Point b = points.get(at + 1);
                    arrowHead(g, a.x, a.y, b.x - a.x, b.y - a.y, new Color(0, 132, 119, 190));
                }
            }
        }
    }

    private static void drawTestVector(Graphics2D g, Dimension size, double x, double y, double ex, double ey) {
        int px = screenX(x, size);
        int py = screenY(y, size);
        g.setColor(new Color(186, 112, 18, 50));
        g.fillOval(px - 10, py - 10, 20, 20);
        g.setColor(Theme.YELLOW);
        g.setStroke(new BasicStroke(1.8f));
        g.drawOval(px - 8, py - 8, 16, 16);
        g.setColor(Theme.TEXT);
        g.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g.drawString("P", px + 9, py - 7);
        double magnitude = Math.hypot(ex, ey);
        if (Double.isFinite(magnitude) && magnitude > 1e-9) {
            double length = Math.min(62, 18 + 9 * Math.log10(1 + magnitude));
            double dx = ex / magnitude * length;
            double dy = -ey / magnitude * length;
            g.setColor(Theme.YELLOW);
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(px, py, px + (int) dx, py + (int) dy);
            arrowHead(g, px + (int) dx, py + (int) dy, dx, dy, Theme.YELLOW);
        }
    }

    private static void drawCharges(Graphics2D g, Dimension size, double[] q, double[] xs, double[] ys) {
        for (int i = 0; i < q.length; i++) {
            int x = screenX(xs[i], size);
            int y = screenY(ys[i], size);
            Color color = q[i] > 0 ? Theme.RED : q[i] < 0 ? Theme.BLUE : Theme.SUBTLE;
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 38));
            g.fillOval(x - 14, y - 14, 28, 28);
            g.setColor(color);
            g.setStroke(new BasicStroke(1.8f));
            g.drawOval(x - 11, y - 11, 22, 22);
            g.setFont(new Font("Segoe UI", Font.BOLD, 15));
            String sign = q[i] > 0 ? "+" : q[i] < 0 ? "−" : "0";
            g.drawString(sign, x - 4, y + 5);
            g.setFont(new Font("Segoe UI", Font.BOLD, 8));
            g.drawString("q" + (i + 1) + " " + Ui.number(q[i]) + " nC", x - 20, y + 23);
        }
    }

    private static void drawLegend(Graphics2D g) {
        g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        g.setColor(new Color(0, 132, 119, 190));
        g.drawLine(45, 12, 59, 12);
        g.setColor(Theme.MUTED);
        g.drawString("líneas de campo", 62, 15);
        g.setColor(Theme.BLUE);
        g.drawLine(145, 12, 158, 12);
        g.setColor(Theme.MUTED);
        g.drawString("+V", 161, 15);
        g.setColor(Theme.RED);
        g.drawLine(183, 12, 196, 12);
        g.setColor(Theme.MUTED);
        g.drawString("−V", 199, 15);
        g.setColor(Theme.YELLOW);
        g.drawLine(220, 12, 234, 12);
        g.setColor(Theme.MUTED);
        g.drawString("E en P", 237, 15);
    }

    private static void arrowHead(Graphics2D g, double x, double y, double dx, double dy, Color color) {
        double magnitude = Math.hypot(dx, dy);
        if (magnitude < 1) return;
        double ux = dx / magnitude;
        double uy = dy / magnitude;
        int size = 5;
        g.setColor(color);
        g.drawLine((int) x, (int) y, (int) (x - size * (ux * .87 - uy * .5)), (int) (y - size * (uy * .87 + ux * .5)));
        g.drawLine((int) x, (int) y, (int) (x - size * (ux * .87 + uy * .5)), (int) (y - size * (uy * .87 - ux * .5)));
    }
}
