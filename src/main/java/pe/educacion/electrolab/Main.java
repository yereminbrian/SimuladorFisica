package pe.educacion.electrolab;

import pe.educacion.electrolab.ui.ElectroLabFrame;

import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ElectroLabFrame frame = new ElectroLabFrame();
            frame.setVisible(true);
        });
    }
}
