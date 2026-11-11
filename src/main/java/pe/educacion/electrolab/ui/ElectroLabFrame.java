package pe.educacion.electrolab.ui;

import pe.educacion.electrolab.modules.CapacitorScreen;
import pe.educacion.electrolab.modules.ChargesScreen;
import pe.educacion.electrolab.modules.CurrentScreen;
import pe.educacion.electrolab.modules.DcCircuitsScreen;
import pe.educacion.electrolab.modules.MagnetismScreen;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public final class ElectroLabFrame extends JFrame {
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pages = new JPanel(pageLayout);
    private final List<NavButton> navigation = new ArrayList<>();

    public ElectroLabFrame() {
        super("ElectroLab | Simulador de electricidad y magnetismo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 730));
        setSize(1300, 830);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);
        setContentPane(root);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        content.add(buildTopBar(), BorderLayout.NORTH);
        pages.setBackground(Theme.BACKGROUND);
        addPage("cargas", new ChargesScreen());
        addPage("capacitores", new CapacitorScreen());
        addPage("corriente", new CurrentScreen());
        addPage("circuitos", new DcCircuitsScreen());
        addPage("magnetismo", new MagnetismScreen());
        content.add(pages, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);
        selectPage(0);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 22));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(250, 700));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.LINE),
                BorderFactory.createEmptyBorder(25, 18, 20, 17)));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel("E", SwingConstants.CENTER);
        mark.setOpaque(true);
        mark.setBackground(new Color(225, 246, 242));
        mark.setForeground(Theme.TEAL);
        mark.setFont(new Font("Segoe UI", Font.BOLD, 19));
        mark.setPreferredSize(new Dimension(43, 43));
        mark.setBorder(BorderFactory.createLineBorder(new Color(174, 222, 216), 1, true));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new javax.swing.BoxLayout(brandText, javax.swing.BoxLayout.Y_AXIS));
        brandText.add(Ui.label("ElectroLab", 19, true, Theme.TEXT));
        Ui.addGap(brandText, 2);
        brandText.add(Ui.label("LABORATORIO VIRTUAL", 9, true, Theme.SUBTLE));
        brand.add(mark, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new javax.swing.BoxLayout(menu, javax.swing.BoxLayout.Y_AXIS));
        menu.add(Ui.eyebrow("Explora los temas"));
        Ui.addGap(menu, 11);
        String[][] items = {
                {"01", "Cargas y campo eléctrico"},
                {"02", "Capacitancia y dieléctricos"},
                {"03", "Corriente, resistencia y fem"},
                {"04", "Circuitos de corriente directa"},
                {"05", "Campo magnético y fuerzas"}
        };
        for (int i = 0; i < items.length; i++) {
            NavButton button = new NavButton(items[i][0], items[i][1], i);
            navigation.add(button);
            menu.add(button);
            Ui.addGap(menu, 6);
        }

        JPanel lower = new JPanel();
        lower.setOpaque(false);
        lower.setLayout(new javax.swing.BoxLayout(lower, javax.swing.BoxLayout.Y_AXIS));
        Ui.CardPanel info = new Ui.CardPanel(new Color(232, 247, 244), new Color(185, 225, 219), 14);
        info.setLayout(new javax.swing.BoxLayout(info, javax.swing.BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(13, 13, 13, 13));
        info.add(Ui.label("Aprende experimentando", 12, true, Theme.TEAL));
        Ui.addGap(info, 5);
        JTextArea text = new JTextArea("Modifica un valor y observa cómo responde el sistema físico.");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        text.setForeground(Theme.MUTED);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setFocusable(false);
        text.setOpaque(false);
        text.setBorder(BorderFactory.createEmptyBorder());
        text.setRows(3);
        text.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        text.setAlignmentX(LEFT_ALIGNMENT);
        info.add(text);
        lower.add(info);
        Ui.addGap(lower, 13);
        lower.add(Ui.label("5 SIMULADORES  ·  UNIDADES SI", 9, true, Theme.SUBTLE));

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(menu, BorderLayout.CENTER);
        sidebar.add(lower, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BACKGROUND);
        top.setBorder(BorderFactory.createEmptyBorder(18, 30, 0, 30));
        JLabel trail = Ui.label("FÍSICA  /  ELECTRICIDAD Y MAGNETISMO", 10, true, Theme.SUBTLE);
        top.add(trail, BorderLayout.WEST);
        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        status.setOpaque(false);
        JLabel dot = Ui.label("●", 9, true, Theme.GREEN);
        JLabel state = Ui.label("SIMULACIÓN LISTA", 9, true, Theme.MUTED);
        status.add(dot);
        status.add(state);
        top.add(status, BorderLayout.EAST);
        return top;
    }

    private void addPage(String key, ModuleScreen screen) {
        JScrollPane scroll = new JScrollPane(screen,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Theme.BACKGROUND);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.getViewport().setOpaque(true);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getVerticalScrollBar().setBackground(Theme.BACKGROUND);
        pages.add(scroll, key);
    }

    private void selectPage(int index) {
        String[] keys = {"cargas", "capacitores", "corriente", "circuitos", "magnetismo"};
        pageLayout.show(pages, keys[index]);
        for (int i = 0; i < navigation.size(); i++) {
            navigation.get(i).setActive(i == index);
        }
    }

    private static String wrapNavigationTitle(String title) {
        return switch (title) {
            case "Cargas y campo eléctrico" -> "Cargas y campo\neléctrico";
            case "Capacitancia y dieléctricos" -> "Capacitancia y\ndieléctricos";
            case "Corriente, resistencia y fem" -> "Corriente, resistencia\ny fem";
            case "Circuitos de corriente directa" -> "Circuitos de corriente\ndirecta";
            case "Campo magnético y fuerzas" -> "Campo magnético\ny fuerzas";
            default -> title;
        };
    }

    private final class NavButton extends JButton {
        private final int index;
        private boolean selected;
        private boolean hovered;

        private NavButton(String number, String title, int index) {
            super("");
            this.index = index;
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setForeground(Theme.MUTED);
            setBackground(Theme.SIDEBAR);
            setPreferredSize(new Dimension(215, 52));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            setMinimumSize(new Dimension(210, 52));
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(0, 13, 0, 8));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);
            setLayout(new BorderLayout(11, 0));
            JLabel num = Ui.label(number, 10, true, Theme.SUBTLE);
            num.setPreferredSize(new Dimension(22, 26));
            num.setHorizontalAlignment(SwingConstants.CENTER);
            add(num, BorderLayout.WEST);
            installForwarding(num);
            JTextArea titleArea = new JTextArea(wrapNavigationTitle(title));
            titleArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            titleArea.setForeground(Theme.MUTED);
            titleArea.setLineWrap(true);
            titleArea.setWrapStyleWord(true);
            titleArea.setEditable(false);
            titleArea.setFocusable(false);
            titleArea.setOpaque(false);
            titleArea.setBorder(BorderFactory.createEmptyBorder());
            titleArea.setMargin(new java.awt.Insets(7, 0, 0, 0));
            titleArea.setRows(2);
            titleArea.setColumns(18);
            titleArea.setPreferredSize(new Dimension(160, 40));
            add(titleArea, BorderLayout.CENTER);
            installForwarding(titleArea);
            addActionListener(this::pressed);
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent event) {
                    hovered = false;
                    repaint();
                }
            });
            putClientProperty("num", num);
            putClientProperty("titleArea", titleArea);
        }

        private void installForwarding(java.awt.Component child) {
            child.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent event) {
                    if (javax.swing.SwingUtilities.isLeftMouseButton(event)) {
                        NavButton.this.doClick();
                    }
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent event) {
                    java.awt.Point point = javax.swing.SwingUtilities.convertPoint(child, event.getPoint(), NavButton.this);
                    if (!NavButton.this.contains(point)) {
                        hovered = false;
                        repaint();
                    }
                }
            });
        }

        private void pressed(ActionEvent event) {
            selectPage(index);
        }

        private void setActive(boolean selected) {
            this.selected = selected;
            JLabel num = (JLabel) getClientProperty("num");
            JTextArea title = (JTextArea) getClientProperty("titleArea");
            num.setForeground(selected ? Theme.TEAL : Theme.SUBTLE);
            title.setForeground(selected ? Theme.TEXT : Theme.MUTED);
            title.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 11));
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            if (selected || hovered) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
                g.setColor(selected ? new Color(225, 245, 242) : new Color(241, 245, 249));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (selected) {
                    g.setColor(Theme.TEAL);
                    g.fillRoundRect(0, 8, 3, getHeight() - 16, 3, 3);
                }
                g.dispose();
            }
            super.paintComponent(graphics);
        }
    }
}
