package actividad_7.aleatorio;

import java.awt.*;

/**
 * Clase que contiene todas las constantes utilizadas en la simulación
 */
public class Constantes {

    // ========================== PARÁMETROS DEL SISTEMA ==========================
    public static final double ERROR_PERMITIDO = 500;
    public static final double VALOR_T = 1.9665;
    public static final int POLITICA_PRODUCCION = 60;
    public static final double MEDIA_DEMANDA = 80;
    public static final double DESVIACION_DEMANDA = 10;
    public static final double COSTO_FALTANTE_UNITARIO = 800;
    public static final double COSTO_INVENTARIO_UNITARIO = 500;

    // ========================== CONFIGURACIÓN DE TABLA ==========================
    public static final String[] COLUMNAS = {
        "Día", "Inventario inicial (Uds)", "Política de producción (Uds)",
        "Total disponible (Uds)", "Rn", "Demanda (uds)", "Ventas (Uds)",
        "Ventas perdidas (uds)", "Inventario final (Uds)", "Costo faltante ($)",
        "Costo de inventarios ($)", "Costo total ($)"
    };

    public static final int[] ANCHOS_COLUMNAS = {
        50, 140, 150, 140, 70, 100, 100, 120, 130, 150, 170, 150
    };

    // ========================== COLORES DEL TEMA ==========================
    public static final Color COLOR_PRIMARIO = new Color(30, 144, 255);
    public static final Color COLOR_FILA_PAR = Color.WHITE;
    public static final Color COLOR_FILA_IMPAR = new Color(235, 245, 255);
    public static final Color COLOR_FONDO_REPLICA = new Color(255, 255, 200);
    public static final Color COLOR_PANEL_REPLICA = new Color(248, 250, 255);

    // ========================== FUENTES ==========================
    public static final Font FUENTE_GENERAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FUENTE_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FUENTE_TITULO = new Font("Segoe UI Semibold", Font.BOLD, 18);
    public static final Font FUENTE_VALOR = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_REPLICA = new Font("Segoe UI Semibold", Font.BOLD, 16);

    // ========================== COLORES PARA GRÁFICAS ==========================
    public static final Color[] COLORES_REPLICAS = {
        new Color(255, 99, 132),   // Rojo/Rosa
        new Color(54, 162, 235),   // Azul
        new Color(255, 205, 86),   // Amarillo
        new Color(75, 192, 192),   // Verde/Turquesa
        new Color(153, 102, 255)   // Púrpura
    };
}
