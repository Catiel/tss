package actividad_7.predeterminado;

import java.awt.*;

/**
 * Clase que contiene todas las constantes utilizadas en la simulación
 */
public class Constantes {

    // ========================== PARÁMETROS DEL SISTEMA ==========================
    public static final double ERROR_PERMITIDO = 500; // Error máximo permitido en el análisis
    public static final double VALOR_T = 1.9665; // Valor t para intervalos de confianza
    public static int POLITICA_PRODUCCION = 60; // Política de producción (modificable)
    public static final double MEDIA_DEMANDA = 80; // Media de la demanda diaria
    public static final double DESVIACION_DEMANDA = 10; // Desviación estándar de la demanda
    public static final double COSTO_FALTANTE_UNITARIO = 800; // Costo unitario por faltante
    public static final double COSTO_INVENTARIO_UNITARIO = 500; // Costo unitario de inventario

    // ========================== OPCIONES DE POLÍTICA ==========================
    public static final int[] OPCIONES_POLITICA = {60, 70, 80}; // Opciones de política de producción

    // ========================== CONFIGURACIÓN DE TABLA ==========================
    public static final String[] COLUMNAS = {
        "Día", "Inventario inicial (Uds)", "Política de producción (Uds)",
        "Total disponible (Uds)", "Rn", "Demanda (uds)", "Ventas (Uds)",
        "Ventas perdidas (uds)", "Inventario final (Uds)", "Costo faltante ($)",
        "Costo de inventarios ($)", "Costo total ($)"
    }; // Nombres de columnas de la tabla principal

    public static final int[] ANCHOS_COLUMNAS = {
        50, 140, 150, 140, 70, 100, 100, 120, 130, 150, 170, 150
    }; // Anchos preferidos de cada columna

    // ========================== COLORES DEL TEMA ==========================
    public static final Color COLOR_PRIMARIO = new Color(30, 144, 255); // Azul principal
    public static final Color COLOR_FILA_PAR = Color.WHITE; // Color para filas pares
    public static final Color COLOR_FILA_IMPAR = new Color(235, 245, 255); // Color para filas impares
    public static final Color COLOR_FONDO_REPLICA = new Color(255, 255, 200); // Fondo para datos de réplicas
    public static final Color COLOR_PANEL_REPLICA = new Color(248, 250, 255); // Fondo para panel de réplica

    // ========================== FUENTES ==========================
    public static final Font FUENTE_GENERAL = new Font("Segoe UI", Font.PLAIN, 14); // Fuente general
    public static final Font FUENTE_HEADER = new Font("Segoe UI", Font.BOLD, 15); // Fuente para encabezados
    public static final Font FUENTE_TITULO = new Font("Segoe UI Semibold", Font.BOLD, 18); // Fuente para títulos
    public static final Font FUENTE_VALOR = new Font("Segoe UI", Font.BOLD, 14); // Fuente para valores destacados
    public static final Font FUENTE_REPLICA = new Font("Segoe UI Semibold", Font.BOLD, 16); // Fuente para panel de réplica

    // ========================== COLORES PARA GRÁFICAS ==========================
    public static final Color[] COLORES_REPLICAS = {
        new Color(255, 99, 132),   // Rojo/Rosa
        new Color(54, 162, 235),   // Azul
        new Color(255, 205, 86),   // Amarillo
        new Color(75, 192, 192),   // Verde/Turquesa
        new Color(153, 102, 255)   // Púrpura
    }; // Colores para distinguir réplicas en gráficas

    /**
     * Método para cambiar la política de producción
     */
    public static void setPoliticaProduccion(int nuevaPolitica) {
        POLITICA_PRODUCCION = nuevaPolitica; // Actualiza la política de producción
    }
}