package actividad_3.ejercicio_1;

import javax.swing.*;
import java.awt.*;

public class EstilosUI {
    public static void aplicarEstiloBoton(JButton boton) {
        boton.setBackground(new Color(60, 130, 200));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarEstiloPanel(JPanel panel) {
        panel.setBackground(new Color(255, 255, 255));
    }

    public static void aplicarEstiloTabla(JTable tabla) {
        tabla.getTableHeader().setBackground(new Color(60, 130, 200));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.setSelectionBackground(new Color(200, 230, 255));
        tabla.setSelectionForeground(Color.BLACK);
    }

    public static void aplicarEstiloLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(new Color(40, 60, 80));
    }

    public static void aplicarEstiloTitulo(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(new Color(30, 90, 160));
    }
}

